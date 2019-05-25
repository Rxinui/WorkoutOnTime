package s3a.iut.prototypechrono;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class TimerAdapter extends ArrayAdapter<String> {

    private List<String> data;

    private int listeRessourceLayout;

    private Context context;

    /**
     * Construit un adapter pour la ListView.
     *
     * @param context
     * @param ressource
     * @param data
     */
    public TimerAdapter(Context context, int ressource, List<String> data){
        super(context,ressource,data);
        this.data = data;
        this.listeRessourceLayout = ressource;
        this.context = context;
    }

    /**
     * Retourne la vue séléctionné dans l'adapteur.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if (null==convertView){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(listeRessourceLayout,parent,false);
            final String timer = data.get(position);
            if (timer!=null){
                final TextView nom_session = (TextView) convertView.findViewById(R.id.lib_list_nomSession);
                final TextView nbExo = (TextView) convertView.findViewById(R.id.lib_list_nbExo);
                final TextView duree = (TextView) convertView.findViewById(R.id.lib_list_duree);
                final CheckBox cb = (CheckBox) convertView.findViewById(R.id.monCheckBox);
                String[] texte = timer.split("/");
                nom_session.setText(texte[0]);
                nbExo.setText(texte[1]);
                duree.setText(texte[2]);
                cb.setChecked(false);
                cb.setTag("checkbox"+position);
            }
        }return convertView;
    }

    /**
     * Retourne le nombre d'élément dans l'adapter.
     *
     * @return le nombre d'élément
     */
    @Override
    public int getCount(){
        if (data != null) return data.size();
        return 0;
    }
}
