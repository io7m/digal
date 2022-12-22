#!/bin/bash -ex

exec > >(tee build.txt) 2>&1

#---------------------------------------------------------------------
# Install all of the various required packages.
#
# We use:
#   xvfb    to provide a virtual X server
#   fluxbox to provide a bare-minimum window manager with click-to-focus
#   ffmpeg  to record the session
#   feh     to set a background
#   xterm   to display the build log in the video
#

sudo apt-get -y update
sudo apt-get -y upgrade
sudo apt-get -y install xvfb fluxbox feh ffmpeg xterm

#---------------------------------------------------------------------
# Start Xvfb on a new display.
#

Xvfb :99 &
export DISPLAY=:99
sleep 1

#---------------------------------------------------------------------
# Start fluxbox on the X server.
#

fluxbox &
sleep 1

#---------------------------------------------------------------------
# Start an xterm that displays the build log.
#

xterm -geometry 200x40 -e tail -F build.txt &
sleep 1

#---------------------------------------------------------------------
# Set a desktop image.
#

feh --bg-tile .github/workflows/wallpaper.jpg
sleep 1

#---------------------------------------------------------------------
# Start recording the session.
#

ffmpeg -f x11grab -y -r 15 -video_size 1280x1024 -i :99 -vcodec libx264 test-suite.mkv &
FFMPEG_PID="$!"
FFMPEG_JOB_ID=$(jobs -l | grep " ${FFMPEG_PID} " | cut -c2)

#---------------------------------------------------------------------
# Execute the passed-in build command.
#

"$@"

#---------------------------------------------------------------------
# Wait a while, and then instruct ffmpeg to stop recording. This step
# is necessary because video files need to be processed when recording
# stops.
#

sleep 10
kill -INT "${FFMPEG_PID}"
fg "${FFMPEG_JOB_ID}"
