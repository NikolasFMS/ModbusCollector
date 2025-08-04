set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

SLAVE_PID_FILE="/tmp/modbus_test_slave.pid"

cleanup() {
    echo -e "\n${YELLOW}Остановка тестовой среды...${NC}"

    if [ -f "$SLAVE_PID_FILE" ]; then
        SLAVE_PID=$(cat "$SLAVE_PID_FILE")
        if ps -p $SLAVE_PID > /dev/null 2>&1; then
            echo -e "${YELLOW}Остановка тестового сервера (PID: $SLAVE_PID)...${NC}"
            kill $SLAVE_PID 2>/dev/null || true
            sleep 2
            if ps -p $SLAVE_PID > /dev/null 2>&1; then
                kill -9 $SLAVE_PID 2>/dev/null || true
            fi
        fi
        rm -f "$SLAVE_PID_FILE"
    fi

    echo -e "${GREEN}Тестовая среда остановлена.${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

echo -e "${BLUE}Проверка зависимостей...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}Ошибка: Java не установлена или отсутствует в PATH${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
if [[ ! $JAVA_VERSION == 1.8* ]]; then
    echo -e "${YELLOW}Предупреждение: Для этого приложения рекомендуется Java 8${NC}"
    echo "Текущая версия Java: $JAVA_VERSION"
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Ошибка: Maven не установлен или отсутствует в PATH${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Проверка зависимостей пройдена${NC}"

echo -e "${YELLOW}Компиляция...${NC}"
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo -e "${RED}Компиляция не удалась!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Проект успешно скомпилирован${NC}"

if command -v lsof &> /dev/null; then
    if lsof -i :5002 &> /dev/null; then
        echo -e "${YELLOW}Предупреждение: Порт 5002 уже используется. Это может помешать тестированию.${NC}"
        read -p "Продолжить? (y/N): " -r
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
fi

echo ""
echo -e "${BLUE}Шаг 1: Запуск тестового сервера Modbus...${NC}"
echo -e "${CYAN}Конфигурация сервера:${NC}"
echo "  - Протокол: Modbus TCP"
echo "  - Адрес: 127.0.0.1:5002"
echo "  - Регистры: 512-531 (20 регистров)"
echo "  - Данные: Случайные значения типа float, обновляемые каждую секунду"

nohup mvn exec:java -Dexec.mainClass="me.ildarorama.modbuscollector.support.TestSlave" -q > /tmp/modbus_slave.log 2>&1 &
SLAVE_PID=$!
echo $SLAVE_PID > "$SLAVE_PID_FILE"

echo -e "${GREEN}✓ Тестовый сервер запущен (PID: $SLAVE_PID)${NC}"

echo -e "${YELLOW}Ожидание инициализации сервера...${NC}"
sleep 3

if ! ps -p $SLAVE_PID > /dev/null 2>&1; then
    echo -e "${RED}Ошибка: Не удалось запустить тестовый сервер${NC}"
    echo "Проверьте лог-файл: /tmp/modbus_slave.log"
    cleanup
fi

echo -e "${GREEN}✓ Тестовый сервер работает${NC}"

echo ""
echo -e "${BLUE}Шаг 2: Запуск приложения ModbusCollector в тестовом режиме...${NC}"
echo -e "${CYAN}Конфигурация приложения:${NC}"
echo "  - Режим: Тестовый режим (TCP-соединение)"
echo "  - Цель: 127.0.0.1:5002"
echo "  - Конфигурация последовательного порта не требуется"

echo ""
echo -e "${GREEN}=== Запуск приложения ===${NC}"
echo -e "${YELLOW}Окно приложения JavaFX должно скоро открыться...${NC}"
echo -e "${YELLOW}Нажмите Ctrl+C в этом терминале, чтобы остановить сервер и клиент${NC}"
echo ""

mvn exec:java -Dexec.mainClass="me.ildarorama.modbuscollector.ModbusCollectorApplication" -Dtest=y

cleanup
