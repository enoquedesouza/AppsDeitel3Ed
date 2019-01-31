package deitel.exemplos.weatherviewer;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Created by Enoque on 29/01/2019.
 *
 *  A classe extend ArrayAdapter, a idéia é é personalizar um elemento do ListView para que possua um layout
 *  onde seja possível exibir as informações recuperadas do web service. Só que para mexer nos views de um ListView
 *  é preciso personalizar o seu adaptador, que o objeto que exibirá os views com um formato específico, sobreescrevendo
 *  o método getView  chamado pelo ListView quando seu adpatodr é setado.
 *
 *
 */

public class WeatherArrayAdapter extends ArrayAdapter<Weather> {


    //Holder do ArrayAdapter é quem gerencia os elementos view de um widget
    //é através do holder que os elementos view do arrayadapter são manipulados
    private static class ViewHolder{

        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView hitTextView;
        TextView humidityTextView;

    }

    private Map<String, Bitmap> bitmaps = new HashMap<>(); //Mapeia um ícone para seu identificado string (nome)
                                                            // Armenada as imagens para que possam ser usada futuramente sem
                                                            //sem precisar baixar novamente

    /*
    *
    * @param context, contexto da atividade onde os views serão exibidos
    * @param forecast, lista de dados Weather (tempo ou clima) que o adaptador será responsável de anexar
    * ao ListView.
    *
    */

    private Context context;
    public WeatherArrayAdapter(Context context, List<Weather> lista){

        super(context, -1, lista); //chama o construtor a superclasse
        this.context = context;

    }


    //Sobreescrita do método getView chamado pelo ListView quando seu adaptador é setado.
    //Define o formato do view que será exibido em cada item do ListView.

    @Override
    public View getView(int position, View convertView, ViewGroup parent){


        Weather day = getItem(position);
        ViewHolder viewHolder;

        if(convertView == null){

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.conditionImageView = (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView = (TextView) convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView = (TextView) convertView.findViewById(R.id.lowTextView);
            viewHolder.hitTextView = (TextView) convertView.findViewById(R.id.hitTextView);
            viewHolder.humidityTextView = (TextView) convertView.findViewById(R.id.humidityTextView);
            convertView.setTag(viewHolder);

        }else{

            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(bitmaps.containsKey(day.iconUrl)){

            viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconUrl));

        }else{

           new LoadImageTask(viewHolder.conditionImageView).execute(day.iconUrl);
        }

        Context context = getContext();

        viewHolder.dayTextView.setText(context.getString(R.string.day_description,
                                        day.dayOfWeek, day.description));

        viewHolder.lowTextView.setText(context.getString(R.string.low_temp, day.minTemp));

        viewHolder.hitTextView.setText(context.getString(R.string.high_temp, day.maxTemp));

        viewHolder.humidityTextView.setText(context.getString(R.string.humidity, day.humidity));


        return convertView;

    }

    //Tarefa excutado a parte para baixar a imagem que representa as condições climática
    // a tarefa é exectada em uma thread separada e depois o resultado do procssamento é enviado
    //para a GUI Thread para que a interface gráfica seja atualizada.
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap>{

        private  ImageView loadImageView;

        public LoadImageTask(ImageView loadImageView){

            this.loadImageView = loadImageView;
        }


        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try{

                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                try(InputStream input = connection.getInputStream()){

                    bitmap = BitmapFactory.decodeStream(input);
                    bitmaps.put(params[0], bitmap);

                } catch (Exception e){

                    e.printStackTrace();
                }

            }catch (Exception e){

                e.printStackTrace();
            }finally {

                connection.disconnect();
            }


            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){

            loadImageView.setImageBitmap(bitmap);

        }
    }

}
