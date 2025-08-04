set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}Приложение ModbusCollector - Тестовый режим${NC}"
echo "======================================="

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
if [[ ! $JAVA_VERSION == 1.8* ]]; then
    echo -e "${YELLOW}Предупреждение: Для этого приложения рекомендуется Java 8${NC}"
    echo "Текущая версия Java: $JAVA_VERSION"
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Ошибка: Maven не установлен или отсутствует в PATH${NC}"
    exit 1
fi

if [ ! -d "target/classes" ]; then
    echo -e "${YELLOW}Проект не скомпилирован. Выполняется компиляция...${NC}"
    mvn clean compile
fi

echo -e "${GREEN}Запуск ModbusCollector в ТЕСТОВОМ РЕЖИМЕ...${NC}"
echo -e "${YELLOW}Конфигурация тестового режима:${NC}"
echo "  - Подключение к TCP-серверу Modbus по адресу 127.0.0.1:5002"
echo "  - Конфигурация последовательного порта не требуется"
echo "  - Убедитесь, что тестовый сервер запущен!"
echo ""
echo -e "${BLUE}Для запуска тестового сервера выполните: ./run-test-slave.sh${NC}"
echo ""

mvn exec:java -Dexec.mainClass="me.ildarorama.modbuscollector.ModbusCollectorApplication" -Dtest=y
