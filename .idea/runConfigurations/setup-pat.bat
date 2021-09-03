@echo off

echo ############################
echo #       PAT RECEIVER       #
echo ############################

echo.
echo Looking for Personal Access Token (PAT) ...

goto check

:setup

echo Eh, GitHub forced it, I cannot do anything :sadface:
echo I hope this script will help ease it a bit
echo Made by TachibanaYui with love :heart: (Ew cringe!)

echo.

echo .pat not found! Asking for user information...
echo.

echo Don't worry, you only need to do this one, or at least occasionally
echo Firstly I need your Github username, Note: Username is different than your profile name
echo You can find your username at:
echo  - Visit https://github.com
echo  - Click your profile picture at the top right corner
echo  - Click your profile
echo  - Look at the url bar, your username should be after "https://github.com/"
echo Example: https://github.com/tachibanayui then "tachibanayui" is my github username
echo.

set /p ghu=Enter your GitHub username here:

echo One more thing, your Personal Access Token (PAT). This is required to send api requests to GitHub
echo.

echo To get one, goto "https://github.com/settings/tokens/new?description=Get%%20Packages%%20from%20ZAP&scopes=read%%3Apackages"
echo I recommend to set your personal access token to "No expiration" so you don't need to do this again after your PAT expires.
echo You can change expriation date under "Expiration *"
echo !!! IMPORTANT: IF YOU DO NOT USE "No expiration" THEN AFTER YOUR PAT EXPIRES, RUN invalidate-pat.bat or Invalidate PAT inside intelliJ
echo Scroll down and click "Generate token"
echo After you redirected to https://github.com/settings/tokens, you should see a token similar to "ghp_xxxxxxxxxxxxxxxx"
echo Click the clipboard icon to Copy and paste it here!
echo.

set /p pat=Enter your Personal Access Token (PAT) here:

echo Finally do you want to save this pat globally so you won't need to do this again for every single project
echo Location: %userprofile%\zap\.pat

set /p global=Save to PAT globally (y/N):

echo Generating .pat file...

echo %ghu%>>.pat
echo %pat%>>.pat

if %global% == y (
if not exist %userprofile%/zap (mkdir %userprofile%\zap)
echo %ghu%>>%userprofile%\zap\.pat
echo %pat%>>%userprofile%\zap\.pat
)

goto check


:check

if not exist .pat (
if not exist %userprofile%\zap\.pat goto setup
echo Found global pat, copying...
copy %userprofile%\zap\.pat .pat
)
echo All set, Have fun coding!