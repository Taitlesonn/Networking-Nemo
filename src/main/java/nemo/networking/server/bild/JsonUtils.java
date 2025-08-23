package nemo.networking.server.bild;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Klasa JsonUtils jest uniwersalnym narzędziem do pracy z danymi JSON przy użyciu biblioteki Gson.
 *
 * Funkcje tej klasy obejmują:
 * 1. Wczytywanie JSON z pliku lub Stringa jako uniwersalny JsonElement.
 * 2. Mapowanie JSON do obiektów dowolnej klasy, list lub map.
 * 3. Zapisywanie obiektów Java do JSON w formacie czytelnym dla człowieka (pretty-print).
 *
 * Zastosowanie:
 * - Uniwersalne parsowanie danych JSON bez konieczności wcześniejszego definiowania pełnej struktury.
 * - Obsługa dynamicznych struktur danych, np. konfiguracji, odpowiedzi API, plików konfiguracyjnych.
 * - Łatwe konwertowanie obiektów Java do JSON i odwrotnie.
 *
 * Klasa jest w pełni statyczna, co umożliwia szybkie i proste użycie bez potrzeby tworzenia instancji.
 */
public class JsonUtils {

    /**
     * Główna instancja Gson wykorzystywana we wszystkich metodach.
     * Zawiera ustawienie PrettyPrinting, aby wygenerowany JSON był czytelny dla człowieka.
     * Możesz modyfikować tę instancję, np. dodając własne adaptery lub strategie serializacji.
     */
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /** -------------------- Wczytywanie JSON -------------------- */

    /**
     * Wczytuje plik JSON i zwraca go jako JsonElement.
     *
     * JsonElement jest uniwersalnym kontenerem JSON w Gson i może reprezentować:
     * - obiekt JSON (JsonObject)
     * - tablicę JSON (JsonArray)
     * - wartość prymitywną (JsonPrimitive)
     * - wartość null (JsonNull)
     *
     * @param filePath Ścieżka do pliku JSON na dysku.
     * @return JsonElement reprezentujący dane JSON w pełnej strukturze.
     * @throws IOException jeśli wystąpi problem z odczytem pliku.
     */
    public static JsonElement readJsonFile(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return JsonParser.parseReader(reader);
        }
    }

    /**
     * Parsuje JSON zapisany w postaci Stringa do uniwersalnego JsonElement.
     *
     * Przydatne w sytuacjach, gdy JSON pochodzi np. z API, bazy danych lub dynamicznego źródła.
     *
     * @param jsonString Tekst zawierający dane w formacie JSON.
     * @return JsonElement reprezentujący strukturę JSON.
     */
    public static JsonElement parseJson(String jsonString) {
        return JsonParser.parseString(jsonString);
    }

    /**
     * Wczytuje plik JSON i mapuje go bezpośrednio na obiekt klasy T.
     *
     * @param filePath Ścieżka do pliku JSON.
     * @param type Klasa, do której JSON zostanie zmapowany.
     * @param <T> Typ obiektu docelowego.
     * @return Obiekt klasy T wypełniony danymi z JSON.
     * @throws IOException jeśli wystąpi problem z odczytem pliku.
     */
    public static <T> T readJsonFile(String filePath, Class<T> type) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, type);
        }
    }

    /**
     * Parsuje JSON zapisany w Stringu do obiektu klasy T.
     *
     * @param jsonString Tekst JSON.
     * @param type Klasa docelowa.
     * @param <T> Typ docelowy.
     * @return Obiekt klasy T z danymi z JSON.
     */
    public static <T> T parseJson(String jsonString, Class<T> type) {
        return gson.fromJson(jsonString, type);
    }

    /**
     * Parsuje JSON String do listy obiektów klasy T.
     * Przydatne, gdy JSON reprezentuje tablicę obiektów.
     *
     * @param jsonString JSON w postaci tablicy.
     * @param type Klasa elementów listy.
     * @param <T> Typ elementów listy.
     * @return Lista obiektów typu T.
     */
    public static <T> List<T> parseJsonList(String jsonString, Class<T> type) {
        Type listType = TypeToken.getParameterized(List.class, type).getType();
        return gson.fromJson(jsonString, listType);
    }

    /**
     * Parsuje JSON String do mapy (Map<String, Object>).
     *
     * Pozwala na dynamiczne odczytywanie nieznanej struktury JSON.
     * Umożliwia łatwy dostęp do wartości po kluczach bez definiowania klasy.
     *
     * @param jsonString Tekst JSON.
     * @return Mapa z kluczami typu String i wartościami typu Object.
     */
    public static Map<String, Object> parseJsonMap(String jsonString) {
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(jsonString, mapType);
    }

    /** -------------------- Zapisywanie JSON -------------------- */

    /**
     * Zapisuje obiekt Java do pliku JSON w formacie czytelnym dla człowieka.
     *
     * Obsługuje wszystkie typy obiektów, w tym mapy, listy i klasy niestandardowe.
     *
     * @param obj Obiekt do zapisania.
     * @param filePath Ścieżka pliku docelowego.
     * @throws IOException jeśli wystąpi problem z zapisem.
     */
    public static void writeJsonToFile(Object obj, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(obj, writer);
        }
    }

    /**
     * Konwertuje obiekt Java do sformatowanego JSON String.
     * Przydatne do logów, debugowania lub wysyłania danych przez sieć.
     *
     * @param obj Obiekt do konwersji.
     * @return Tekst JSON w formacie Pretty Print.
     */
    public static String toJsonString(Object obj) {
        return gson.toJson(obj);
    }

}
