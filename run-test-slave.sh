set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}Тестовый сервер Modbus${NC}"
echo "========================"

if ! command -v java &> /dev/null; then
    echo -e "${RED}Ошибка: Java не установлена или отсутствует в PATH${NC}"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Ошибка: Maven не установлен или отсутствует в PATH${NC}"
    exit 1
fi

if [ ! -d "target/classes" ]; then
    echo -e "${YELLOW}Проект не скомпилирован. Выполняется компиляция...${NC}"
    mvn clean compile
fi

echo -e "${GREEN}Запуск тестового сервера Modbus...${NC}"
echo -e "${YELLOW}Сервер будет слушать TCP-порт 5002${NC}"
echo -e "${YELLOW}Регистры 512-531 будут доступны со случайными значениями типа float${NC}"
echo -e "${YELLOW}Нажмите Ctrl+C для остановки сервера${NC}"
echo ""

mvn exec:java -Dexec.mainClass="me.ildarorama.modbuscollector.support.TestSlave" -q

echo -e "${GREEN}Тестовый сервер остановлен.${NC}"
