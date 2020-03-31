import com.mongodb.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoStore {
    private DBCollection markets;
    private DBCollection items;

    MongoStore() {
        MongoClient mongoClient = new MongoClient( "127.0.0.1" , 27017 );
        DB database = mongoClient.getDB("local");

        //инициализируем коллекции магазинов и товаров
        markets = database.getCollection("Markets");
        markets.drop();
        items = database.getCollection("Items");
        items.drop();
    }

    //добавляем магазин
    public void addMarket(String marketName) {
        var market = new BasicDBObject("name", marketName);

        if (markets.find(market).hasNext()) {
            System.out.println("Такой магазин уже есть.");
        }else {
            markets.insert(market.append("items", new ArrayList<String>()));
        }
    }

    //добавить товар
    public void addItem(String itemName, int itemPrice) {
        var item = new BasicDBObject("name", itemName);

        if (items.find(item).hasNext()) {
            System.out.println("Такой товар уже есть.");
        }else {
            items.insert(item.append("price", itemPrice));
        }
    }

    //выставить товар
    public void exposeItem(String item, String market) {
        var itemQuery = new BasicDBObject("name", item);
        var marketQuery = new BasicDBObject("name", market);

        if (items.count(itemQuery) == 0) {
            System.out.println("Товар не найден");
        }else if (markets.count(marketQuery) == 0) {
            System.out.println("Магазин не найден");
        }else {
            var updateQuery = markets.find(marketQuery).next();
            var updateCommand = new BasicDBObject("$push", new BasicDBObject("items", item));
            markets.update(updateQuery, updateCommand);
        }
    }

    public void statistics() {
        DBObject look = new BasicDBObject("$lookup", new BasicDBObject("from", "Items")
                        .append("localField", "items")
                        .append("foreignField", "name")
                        .append("as", "items_list"));
        DBObject unwind = new BasicDBObject("$unwind", new BasicDBObject("path", "$items_list"));
        DBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$name")
                .append("avgprice", new BasicDBObject("$avg", "$items_list.price"))
                .append("minprice", new BasicDBObject("$min", "$items_list.price"))
                .append("maxprice", new BasicDBObject("$max", "$items_list.price"))
                .append("arrayItems", new BasicDBObject("$push", "$items_list.price")));

        List<DBObject> pipeline = Arrays.asList(look, unwind, group);
        var options = AggregationOptions.builder().allowDiskUse(true).batchSize(10000).build();
        var cursor = markets.aggregate(pipeline, options);

        while (cursor.hasNext()) {
            var currentCursor = cursor.next();
            var itemsList = (BasicDBList) currentCursor.get("arrayItems");

            System.out.println("Магазин " + currentCursor.get("_id"));
            System.out.println("\tВсего товаров в магазине: " + itemsList.size());
            System.out.println("\tСредняя цена товаров: " + currentCursor.get("avgprice"));
            System.out.println("\tСамый дорогой товар: " + currentCursor.get("maxprice"));
            System.out.println("\tСамый дешевый товар: " + currentCursor.get("minprice"));
            System.out.println("\tКол-во товаров меньше 100 рублей: " + itemsList.stream()
                    .mapToInt(el -> (int) el)
                    .filter(el -> el < 100)
            .count());
        }
    }
}