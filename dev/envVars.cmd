@set DATABASE_URL="jdbc:sqlite:chocolate_dev.db"
echo DATABASE_URL set to "jdbc:sqlite:chocolate_dev.db"

#@set BROKER_HOST=127.0.0.1
#echo BROKER_HOST set to 127.0.0.1

#@set BROKER_PORT=5672
#echo BROKER_PORT set to 5672

#@set BROKER_USERNAME="guest"
#echo BROKER_USERNAME set to "guest"

#@set BROKER_PASSWORD="guest"
#echo BROKER_PASSWORD set to "guest"


set BROKER_URL="amqp://guest:guest@localhost:5672"
echo BROKER_URL set to "amqp://guest:guest@localhost:5672"

@set BROKER_VHOST="/main"
echo BROKER_VHOST set to "/main"