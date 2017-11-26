/**
 * Created by matt on 26-10-17.
 * interface para comparar las clases, y que permita filtrar
 * ademas que puede sacar la key de la clase
 */

public interface CompareAndFilter<T> {
    int compareTo(T o);
    String getKeyClass();
    boolean filter(String wordKey);
    void setKeyClass(String key);
}
