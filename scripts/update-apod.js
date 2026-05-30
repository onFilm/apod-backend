const fs = require('fs/promises');
const path = require('path');

async function getNewYorkDate() {
  const formatter = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/New_York',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
  // en-CA format gives YYYY-MM-DD natively
  return formatter.format(new Date()); 
}

async function fetchApodWithRetry(date, retries = 3) {
  const url = `https://apod.ellanan.com/api?date=${date}`;
  for (let i = 0; i < retries; i++) {
    try {
      console.log(`Fetching APOD for date: ${date} (Attempt ${i + 1}/${retries})`);
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error(`Error fetching APOD: ${error.message}`);
      if (i === retries - 1) throw error;
      const delay = 2000 * Math.pow(2, i);
      console.log(`Retrying in ${delay}ms...`);
      await new Promise(res => setTimeout(res, delay)); // Exponential backoff
    }
  }
}

async function main() {
  try {
    // 1. Determine target date (Input overrides NY current date)
    const inputDate = process.argv[2];
    const targetDate = (inputDate && inputDate.trim() !== '') ? inputDate.trim() : await getNewYorkDate();
    console.log(`Target Date resolved to: ${targetDate}`);

    const dataFilePath = path.join(__dirname, '..', 'src', 'main', 'resources', 'apod_data.json');
    let existingData = [];
    
    // 4. Read existing JSON file
    try {
      const fileContent = await fs.readFile(dataFilePath, 'utf8');
      if (fileContent.trim() !== '') {
        existingData = JSON.parse(fileContent);
      }
      if (!Array.isArray(existingData)) {
         existingData = [];
      }
    } catch (error) {
      if (error.code === 'ENOENT') {
        console.log('apod_data.json does not exist. A new array will be created.');
      } else {
        console.warn('Could not parse existing apod_data.json, starting fresh.', error.message);
      }
    }

    // 5 & 6. Prevent duplicates by checking if date is already in array
    const exists = existingData.some(entry => entry.date === targetDate);
    const githubOutput = process.env.GITHUB_OUTPUT;
    
    if (exists) {
      // 7. If record exists: log, exit successfully, skip commit
      console.log(`APOD for ${targetDate} already exists. Skipping.`);
      if (githubOutput) {
        await fs.appendFile(githubOutput, `changed=false\napod_date=${targetDate}\n`);
      }
      process.exit(0);
    }

    // 2 & 3. Call APOD API and validate HTTP response
    const apodData = await fetchApodWithRetry(targetDate);

    // 8. If record doesn't exist: append object, sort chronologically, write back
    existingData.push(apodData);
    existingData.sort((a, b) => new Date(a.date) - new Date(b.date));

    // Ensure the directory exists
    await fs.mkdir(path.dirname(dataFilePath), { recursive: true });
    
    // Pretty-print JSON and write
    await fs.writeFile(dataFilePath, JSON.stringify(existingData, null, 2) + '\n', 'utf8');
    console.log(`Successfully appended APOD for ${targetDate}.`);

    // 9. Export outputs for GitHub Actions
    if (githubOutput) {
      await fs.appendFile(githubOutput, `changed=true\napod_date=${targetDate}\n`);
    }
  } catch (error) {
    // 10. Clear logging and error handling
    console.error('Workflow failed:', error);
    process.exit(1);
  }
}

main();