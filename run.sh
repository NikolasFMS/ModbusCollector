set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}Запуск приложения ModbusCollector${NC}"
echo "=================================="

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
if [[ ! $JAVA_VERSION == 1.8* ]]; then
    echo -e "${YELLOW}Предупреждение: Для этого приложения рекомендуется Java 8${NC}"
    echo "Текущая версия Java: $JAVA_VERSION"
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Ошибка: Maven не установлен или отсутствует в PATH${NC}"
    exit 1
fi

echo -e "${GREEN}Компиляция проекта...${NC}"
mvn clean compile

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Компиляция успешно завершена!${NC}"
    echo -e "${GREEN}Запуск приложения ModbusCollector...${NC}"
    echo ""

    mvn exec:java -Dexec.mainClass="me.ildarorama.modbuscollector.ModbusCollectorApplication"
else
    echo -e "${RED}Компиляция не удалась!${NC}"
    exit 1
fi
