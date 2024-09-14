@echo off
chcp 65001  
start chrome.exe^
  "https://web.telegram.org/a/"^
  --no-first-run^
  --start-maximized^
  --disable-extensions^
  --disable-gpu^
  --disable-software-rasterizer^
  --disable-background-networking^
  --disable-sync^
  --disable-translate^
  --disable-renderer-backgrounding^
  --disable-client-side-phishing-detection^
  --disable-hang-monitor^
  --disable-audio-output^
  --disable-accelerated-2d-canvas^
  --enable-low-end-device-mode^
  --enable-simple-cache-backend^
  --disable-quic^
  --disable-infobars^
  --disable-session-crashed-bubble^
  --disable-speech-api^
  --disable-save-password-bubble^
  --disable-notifications^
  --user-data-dir="D:\chrome-user-data\1-8502950634"