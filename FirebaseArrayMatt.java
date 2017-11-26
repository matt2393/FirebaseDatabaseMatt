import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 26-10-17.
 * Clase que manipulara los eventos de FirebaseDatabase de forma generica
 * con posibilidad a filtrado, y ordenacion por otro campo del cual se hizo la consulta
 */

public class FirebaseArrayMatt<T> implements ChildEventListener, ValueEventListener{

    private T classGen;
    private Class<T> model;
    private List<T> arrayClassGen;
    private List<T> arrayFilterClassGen;
    private Query query;
    private ChangeEventListener changeEventListener;
    private int i,index,oldindex;
    private boolean filtrando, ordenado;
    private String wordKey;
    public FirebaseArrayMatt(Query query,Class<T> model, boolean ordenado){
        this.ordenado=ordenado;
        this.query=query;
        this.model=model;
        init();
    }
    private void init(){
        filtrando=false;
        query.addValueEventListener(this);
        query.addChildEventListener(this);
        arrayClassGen=new ArrayList<>();
        arrayFilterClassGen=new ArrayList<>();
    }
    public void removeListeners(){
        query.removeEventListener((ValueEventListener) this);
        query.removeEventListener((ChildEventListener) this);
    }

    public void setChangeEventListener(ChangeEventListener changeEventListener) {
        this.changeEventListener = changeEventListener;
    }

    public boolean isFiltrando() {
        return filtrando;
    }

    public void setFiltrando(boolean filtrando) {
        this.filtrando = filtrando;
        this.wordKey="";
    }

    public String getWordKey() {
        return wordKey;
    }

    public void setWordKey(String wordKey) {
        this.wordKey = wordKey;
        arrayFilterClassGen=new ArrayList<>();
        arrayFilterClassGen.removeAll(arrayClassGen);
        i=0;
        for (T classGenAux: arrayClassGen) {
            if(((CompareAndFilter)classGenAux).filter(this.wordKey)) {
                arrayFilterClassGen.add(i, classGenAux);
                i++;
            }
        }
    }

    public boolean isOrdenado() {
        return ordenado;
    }

    public void setOrdenado(boolean ordenado) {
        this.ordenado = ordenado;
    }

    private int indice(String key){
        for(i=0;i<arrayClassGen.size();i++){
            if(((CompareAndFilter)arrayClassGen.get(i)).getKeyClass().equals(key))
                return i;
        }
        throw new IllegalArgumentException("No exite la Key");
    }
    private int indiceFilter(String key){
        for(i=0;i<arrayFilterClassGen.size();i++){
            if(((CompareAndFilter)arrayFilterClassGen.get(i)).getKeyClass().equals(key))
                return i;
        }
        return -1;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        classGen = dataSnapshot.getValue(model);
        ((CompareAndFilter) classGen).setKeyClass(dataSnapshot.getKey());
        if (arrayClassGen.size() == 0)
            arrayClassGen.add(0, classGen);
        else {
            if(ordenado) {
                for (i = 0; i < arrayClassGen.size(); i++) {
                    if (((CompareAndFilter) classGen).compareTo(arrayClassGen.get(i)) > 0) {
                        arrayClassGen.add(i, classGen);
                        break;
                    }
                }
                if (i == arrayClassGen.size())
                    arrayClassGen.add(i, classGen);

                index = i;
            }
            else{
                index=0;
                if(s!=null)
                    index=indice(s)+1;
                arrayClassGen.add(index,classGen);
            }
        }
        if(!filtrando) {
            changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.ADD, index, -1);
        }
        else{
            if (arrayFilterClassGen.size() == 0)
                arrayFilterClassGen.add(0, classGen);
            else {
                if(((CompareAndFilter) classGen).filter(wordKey)) {
                    if(ordenado) {
                        for (i = 0; i < arrayFilterClassGen.size(); i++) {
                            if (((CompareAndFilter) classGen).compareTo(arrayFilterClassGen.get(i)) > 0) {
                                arrayFilterClassGen.add(i, classGen);
                                break;
                            }
                        }
                        if (i == arrayFilterClassGen.size())
                            arrayFilterClassGen.add(i, classGen);

                        index = i;
                    }
                    else{
                        index=0;
                        if(s!=null)
                            index=indiceFilter(s)+1;
                        arrayFilterClassGen.add(index,classGen);
                    }
                    changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.ADD,index,-1);
                }
            }
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        classGen=dataSnapshot.getValue(model);
        ((CompareAndFilter)classGen).setKeyClass(dataSnapshot.getKey());
        index=indice(dataSnapshot.getKey());
        arrayClassGen.set(index,classGen);
        if(!filtrando){
            if(ordenado){
                oldindex=index;
                for(i=0;i<arrayClassGen.size();i++){
                    if(i!=oldindex && ((CompareAndFilter)classGen).compareTo(arrayClassGen.get(i))>0)
                        break;
                }
                if(i==arrayClassGen.size() && i!=oldindex){
                    arrayClassGen.remove(oldindex);
                    index=i-1;
                    arrayClassGen.add(index,classGen);
                }
                if(i!=arrayClassGen.size() && i!=oldindex){
                    arrayClassGen.remove(oldindex);
                    if(i<oldindex)
                        index=i;
                    else
                        index=i-1;
                    arrayClassGen.add(index,classGen);
                }
                if(i!=oldindex)
                    changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.MOVE,index,oldindex);
                else{
                    changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.CHANGE,index,-1);
                }
            }
            else
                changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.CHANGE,index,-1);
        }
        else{
            index=indiceFilter(dataSnapshot.getKey());
            if(index>-1){
                arrayFilterClassGen.set(index,classGen);
                if(ordenado){
                    oldindex=index;
                    for(i=0;i<arrayFilterClassGen.size();i++){
                        if(i!=oldindex && ((CompareAndFilter)classGen).compareTo(arrayFilterClassGen.get(i))>0)
                            break;
                    }
                    if(i==arrayFilterClassGen.size() && i!=oldindex){
                        arrayFilterClassGen.remove(oldindex);
                        index=i-1;
                        arrayFilterClassGen.add(index,classGen);
                    }
                    if(i!=arrayFilterClassGen.size() && i!=oldindex){
                        arrayFilterClassGen.remove(oldindex);
                        if(i<oldindex)
                            index=i;
                        else
                            index=i-1;
                        arrayFilterClassGen.add(index,classGen);
                    }
                    if(i!=oldindex)
                        changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.MOVE,index,oldindex);
                    else{
                        changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.CHANGE,index,-1);
                    }
                }
                else {
                    changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.CHANGE, index, -1);
                }
            }
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        index=indice(dataSnapshot.getKey());
        arrayClassGen.remove(index);
        if(!filtrando){
            changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.DELETE,index,-1);
        }
        else{
            index=indiceFilter(dataSnapshot.getKey());
            if(index>-1){
                arrayFilterClassGen.remove(index);
                changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.DELETE,index,-1);
            }
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        if(!ordenado) {
            classGen = dataSnapshot.getValue(model);
            ((CompareAndFilter) classGen).setKeyClass(dataSnapshot.getKey());
            oldindex = indice(dataSnapshot.getKey());
            arrayClassGen.remove(oldindex);
            index = indice(s) + 1;
            arrayClassGen.add(index, classGen);
            if (!filtrando) {
                changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.MOVE, index, oldindex);
            } else {
                if (((CompareAndFilter) classGen).filter(wordKey)) {
                    oldindex = indiceFilter(dataSnapshot.getKey());
                    if (oldindex > -1) {
                        arrayFilterClassGen.remove(oldindex);
                        index = indice(s);
                        if (index > -1) {
                            index++;
                            arrayFilterClassGen.add(index, classGen);
                            changeEventListener.onChangeEvent(ChangeEventListener.TipoEvent.MOVE, index, oldindex);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        changeEventListener.onDataChance();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        changeEventListener.onCancelled(databaseError);
    }

    public int Size(){
        if(!filtrando)
            return arrayClassGen.size();
        else
            return arrayFilterClassGen.size();
    }

    public T getClassGen(int in){
        if(!filtrando)
            return arrayClassGen.get(in);
        else
            return arrayFilterClassGen.get(in);
    }
}
