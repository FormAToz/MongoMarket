import java.util.Scanner;

public class Test {

    private static final String ADD_MARKET = "ДОБАВИТЬ_МАГАЗИН";
    private static final String ADD_ITEM = "ДОБАВИТЬ_ТОВАР";
    private static final String EXPOSE = "ВЫСТАВИТЬ_ТОВАР";
    private static final String LIST = "СТАТИСТИКА_ТОВАРОВ";

    public static void main(String[] args) {

        var mongoStore = new MongoStore();

        System.out.println("Доступные команды для управления:\n" +
                ADD_MARKET + " лента\n" +
                ADD_ITEM + " пельмени 100\n" +
                EXPOSE + " пельмени лента\n" +
                LIST);

        //Добавляем магазины и товары для теста
        mongoStore.addMarket("Пятерочка");
        mongoStore.addMarket("Магнит");

        mongoStore.addItem("Вафли", 100);
        mongoStore.addItem("Сахар", 80);
        mongoStore.addItem("Печенье", 67);
        mongoStore.addItem("Торт", 150);
        mongoStore.addItem("Мясо", 90);

        mongoStore.exposeItem("Вафли", "Пятерочка");
        mongoStore.exposeItem("Сахар", "Пятерочка");
        mongoStore.exposeItem("Печенье", "Пятерочка");

        mongoStore.exposeItem("Вафли", "Магнит");
        mongoStore.exposeItem("Сахар", "Магнит");
        mongoStore.exposeItem("Печенье", "Магнит");
        mongoStore.exposeItem("Торт", "Магнит");
        mongoStore.exposeItem("Мясо", "Магнит");

        while (true) {
            System.out.println("Введите команду:");

            var scanner = new Scanner(System.in);
            var input = scanner.nextLine();
            String[] stringArray = input.split("\\s");

            //добавляем магазин
            if (input.matches(ADD_MARKET + "\\s\\S+")){
                var marketName = stringArray[1];
                mongoStore.addMarket(marketName);

            //добавляем товар
            }else if (input.matches(ADD_ITEM + "\\s\\S+\\s\\S+")) {
                var itemName = stringArray[1];
                var itemPrice = Integer.parseInt(stringArray[2]);
                mongoStore.addItem(itemName, itemPrice);

            //выставить товар
            }else if (input.matches(EXPOSE + "\\s\\S+\\s\\S+")) {
                var marketName = stringArray[2];
                var itemName = stringArray[1];
                mongoStore.exposeItem(itemName, marketName);

            //список всех товаров в магазинах
            }else if (input.matches(LIST)) {
                mongoStore.statistics();

            }else {
                System.out.println("Неверный формат ввода");
                continue;
            }
        }
    }
}