import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by matt on 26-10-17.
 * Adaptador generico para un recyclerView
 */

public abstract class FirebaseRecyclerAdapterMatt<T,VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
    private FirebaseArrayMatt<T> firebaseArrayMatt;
    private Class<T> modelClass;
    private Class<VH> viewHolderClass;
    private int modelLayout;


    public FirebaseRecyclerAdapterMatt(Class<T> model,
                                       @LayoutRes int modelLayout,
                                       Class<VH> viewHolder,
                                       Query datos,
                                       boolean ordenado){
        this.modelClass=model;
        this.modelLayout=modelLayout;
        this.viewHolderClass=viewHolder;
        this.firebaseArrayMatt=new FirebaseArrayMatt<>(datos,model,ordenado);

        this.firebaseArrayMatt.setChangeEventListener(new ChangeEventListener() {
            @Override
            public void onChangeEvent(TipoEvent tipoEvent, int index, int indexOld) {
                FirebaseRecyclerAdapterMatt.this.onChildChanged(tipoEvent,index,indexOld);
            }

            @Override
            public void onDataChance() {
                FirebaseRecyclerAdapterMatt.this.onDataChange();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                FirebaseRecyclerAdapterMatt.this.onCancelled(error);
            }
        });
    }

    public void startFilter(){
        firebaseArrayMatt.setFiltrando(true);
    }
    public void stopFilter(){
        firebaseArrayMatt.setFiltrando(false);
    }

    public void filtrar(String wordkey){
        if(firebaseArrayMatt.isFiltrando()) {
            firebaseArrayMatt.setWordKey(wordkey);
            notifyDataSetChanged();
        }
    }

    public void cleanListeners(){
        firebaseArrayMatt.removeListeners();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(viewType,parent,false);
        try {
            Constructor<VH> constructor=viewHolderClass.getConstructor(View.class);
            return constructor.newInstance(view);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        T model=getClassGen(position);
        populateViewHolder(holder,model,position);
    }

    @Override
    public int getItemCount() {
        return firebaseArrayMatt.Size();
    }

    @Override
    public int getItemViewType(int position) {
        return modelLayout;
    }

    public T getClassGen(int pos){
        return firebaseArrayMatt.getClassGen(pos);
    }

    protected void onChildChanged(ChangeEventListener.TipoEvent tipo,int index,int oldindex){
        switch (tipo){
            case ADD:
                notifyItemInserted(index);
                break;
            case CHANGE:
                notifyItemChanged(index);
                break;
            case DELETE:
                notifyItemRemoved(index);
                break;
            case MOVE:
                notifyItemMoved(oldindex,index);
                break;
            default:
                throw new IllegalStateException("Error en tipo de cambio");
        }
    }
    protected void onDataChange(){

    }
    protected void onCancelled(DatabaseError error){
        Log.e("AdapterMatt",error.getMessage());
    }

    protected abstract void populateViewHolder(VH viewHolder, T model, int position);
}
