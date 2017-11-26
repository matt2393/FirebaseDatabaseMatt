import com.google.firebase.database.DatabaseError;

/**
 * Created by matt on 30-09-17.
 * Escucha para los eventos de FirebaseDatabase
 *
 */

public interface ChangeEventListener {
   enum TipoEvent{
       ADD,
       CHANGE,
       MOVE,
       DELETE
    }

    void onChangeEvent(ChangeEventListener.TipoEvent tipoEvent,int index, int indexOld);
    void onDataChance();
    void onCancelled(DatabaseError error);
}
