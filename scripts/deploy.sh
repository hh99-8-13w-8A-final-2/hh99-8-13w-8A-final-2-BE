#!/bin/bash
BUILD_JAR=$(ls /home/ubuntu/app/deploy/podor-*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo "> build 파일명: $JAR_NAME" >> /home/ubuntu/app/deploy/deploy.log

echo "> build 파일 복사" >> /home/ubuntu/app/deploy/deploy.log
DEPLOY_PATH=/home/ubuntu/app/deploy/
cp $BUILD_JAR $DEPLOY_PATH

RESPONSE_CODE=${curl -s -o /dev/null -w "%http_code" http://localhost/api/profile}
echo "> $RESPONSE_CODE response code"

if [ ${RESPONSE_CODE} -ge 400 ]
then
  echo "> NO RUNNING APP"
  CURRENT_PROFILE=port2
else
  CURRENT_PROFILE=$(curl -s http://localhost/api/profile)
fi

echo "> $CURRENT_PROFILE current profile"

if [ $CURRENT_PROFILE == port1 ]
then
  IDLE_PROFILE=port2
elif [ $CURRENT_PROFILE == port2 ]
then
  IDLE_PROFILE=port1
else
  echo "> no coincidence profile: $CURRENT_PROFILE"
  echo "> set profile: port1"
  IDLE_PROFILE=port1
fi

IDLE_APPLICATION=$IDLE_PROFILE-$JAR_NAME
IDLE_APPLICATION_PATH=$DEPLOY_PATH$IDLE_APPLICATION

ln -Tfs $DEPLOY_PATH$JAR_NAME $IDLE_APPLICATION_PATH

echo "> 현재 실행중인 애플리케이션 pid 확인" >> /home/ubuntu/app/deploy/deploy.log

IDLE_PID=$(pgrep -f $IDLE_APPLICATION)

if [ -z $IDLE_PID ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> /home/ubuntu/app/deploy/deploy.log
else
  echo "> kill -15 $IDLE_PID"
  kill -15 $IDLE_PID
  sleep 10
fi

echo "> $IDLE_PROFILE 배포"    >> /home/ubuntu/app/deploy/deploy.log
nohup java -jar -Duser.timezone=GMT+9 -Dspring.profiles.active=$IDLE_PROFILE $IDLE_APPLICATION_PATH >> /home/ubuntu/app/deploy/deploy.log 2>/home/ubuntu/app/deploy/deploy_err.log &

echo "> Profile Switch"
sleep 10
sudo sh /home/ubuntu/app/deploy/switch.sh