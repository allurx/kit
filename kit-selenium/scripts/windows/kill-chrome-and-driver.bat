rem Requests forced termination of all Chrome and ChromeDriver processes and their child processes.
rem Unrelated browser sessions are included; this is not per-test cleanup, and permission failures may leave processes alive.
taskkill /f /t /im chrome.exe & taskkill /f /t /im chromedriver.exe