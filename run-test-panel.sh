
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}ModbusCollector - Тестовая панель управления${NC}"
echo -e "${BLUE}==========================================${NC}"
echo ""
echo "Запуск графической панели управления параметрами..."
echo ""

# Проверка Java
echo -e "${YELLOW}Проверка Java...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java не найдена!${NC}"
    echo ""
    echo "Пожалуйста, установите Java 8+ для запуска приложения:"
    echo ""
    echo "Ubuntu/Debian:"
    echo "  sudo apt update && sudo apt install openjdk-8-jre"
    echo ""
    echo "CentOS/RHEL:"
    echo "  sudo yum install java-1.8.0-openjdk"
    echo ""
    echo "Или скачайте с:"
    echo "  Oracle JDK 8: https://www.oracle.com/java/technologies/javase-jdk8-downloads.html"
    echo "  OpenJDK 8: https://adoptium.net/temurin/releases/?version=8"
    echo ""
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
echo -e "${GREEN}Java найдена: версия $JAVA_VERSION${NC}"

# Проверка Maven
echo -e "${YELLOW}Проверка Maven...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven не найден!${NC}"
    echo ""
    echo "Пожалуйста, установите Apache Maven:"
    echo ""
    echo "Ubuntu/Debian:"
    echo "  sudo apt install maven"
    echo ""
    echo "CentOS/RHEL:"
    echo "  sudo yum install maven"
    echo ""
    echo "Или скачайте с https://maven.apache.org/download.cgi"
    echo ""
    exit 1
fi

MVN_VERSION=$(mvn -version 2>/dev/null | head -n 1 | awk '{print $3}')
echo -e "${GREEN}Maven найден: версия $MVN_VERSION${NC}"

echo -e "${GREEN}Графическое окружение доступно${NC}"

echo ""
echo -e "${YELLOW}🔧 Компиляция проекта...${NC}"
if ! mvn compile -q; then
    echo -e "${RED}Ошибка компиляции!${NC}"
    echo ""
    echo "Проверьте исходный код проекта."
    exit 1
fi

echo -e "${GREEN}Компиляция завершена успешно${NC}"
echo ""

echo -e "${BLUE}Запуск тестовой панели управления...${NC}"
echo ""
echo -e "${BLUE}Сервер будет доступен по адресу: localhost:5002${NC}"
echo -e "${BLUE}Регистры: 512-531 (20 регистров)${NC}"
echo -e "${BLUE}Параметры: 10 (турбокомпрессор)${NC}"
echo ""
echo -e "${YELLOW}Для остановки закройте окно панели управления${NC}"
echo ""

cleanup() {
    echo ""
    echo -e "${YELLOW}Завершение работы...${NC}"
    pkill -f "TestSlaveWithPanel" 2>/dev/null || true
}

trap cleanup EXIT INT TERM

if mvn exec:java -Dexec.mainClass="me.ildarorama.modbuscollector.support.TestSlaveWithPanel" -q; then
    echo ""
    echo -e "${GREEN}Тестовая панель управления завершена корректно${NC}"
else
    EXIT_CODE=$?
    echo ""
    echo -e "${RED}Ошибка при запуске тестовой панели! (код: $EXIT_CODE)${NC}"
    echo ""
    echo -e "${YELLOW}Возможные причины:${NC}"
    echo "  • Порт 5002 уже занят другим приложением"
    echo "  • Проблемы с отображением графического интерфейса"
    echo "  • Недостаточно памяти для запуска JavaFX (встроен в Java 8)"
    echo "  • Проблемы с X11 forwarding (при SSH)"
    echo ""
    echo -e "${BLUE}🔧 Попробуйте:${NC}"
    echo "  1. Закрыть другие Java приложения:"
    echo "     pkill java"
    echo ""
    echo "  2. Проверить доступность порта 5002:"
    echo "     netstat -tlnp | grep 5002"
    echo ""
    echo "  3. Проверить X11 forwarding (при SSH):"
    echo "     echo \$DISPLAY"
    echo "     xset q"
    echo ""
    echo "  4. Убедиться что используется Java 8 (JavaFX встроен):"
    echo "     java -version"
    echo ""
    echo "  5. Увеличить память для Java:"
    echo "     export MAVEN_OPTS=\"-Xmx2g\""
    echo ""
    echo "  6. Запустить в отладочном режиме:"
    echo "     mvn exec:java -Dexec.mainClass=\"me.ildarorama.modbuscollector.support.TestSlaveWithPanel\" -X"
    echo ""

    # Проверка логов
    if [ -f "target/surefire-reports" ]; then
        echo "Проверьте логи в папке target/surefire-reports/"
    fi

    exit $EXIT_CODE
fi

echo ""
echo -e "${BLUE}Завершение работы тестовой панели.${NC}"
