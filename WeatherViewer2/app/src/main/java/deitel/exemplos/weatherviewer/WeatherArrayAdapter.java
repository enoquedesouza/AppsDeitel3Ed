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
 *  Esta class este ArrayAdpater e a personaliza de forma a retorna um view para o ListView
 *  no qual contém as informações climáticas guardadas por um objeto Weather
 *
 */

public class WeatherArrayAdapter extends ArrayAdapter<Weather> {


    //Um Hollder para guardar os views que irão compor o item de ListView
    private static class ViewHolder{

        ImageView conditionImageView; // para o ícone representando as condições
        TextView dayTextView; // para o nome do dia ao se referem as informações
        TextView lowTextView; // para mínima temperatura no dia
        TextView hitTextView; // para a máxima temperatura no dia
        TextView humidityTextView; // para umidade relativa do ar

    }

    private Map<String, Bitmap> bitmaps = new HashMap<>(); //Mapeia um ícone para seu identificado string (nome)
                                                            // Armenada as imagens para que possam ser usada futuramente sem
                                                            //sem precisar baixar novamente

    /*
    *
    * @param context, contexto da atividade onde os views serão exibidos
    * @param forecast, lista de objetos Weather para serem exibidos no ListView
    *
    */


    public WeatherArrayAdapter(Context context, List<Weather> lista){

        super(context, -1, lista); //chama o construtor a superclasse

    }


    //Sobreescrita do método getView chamado pelo ListView quando seu adaptador é setado.
    //Define o formato do view que será exibido em cada item do ListView.

    @Override
    public View getView(int position, View convertView, ViewGroup parent){


        Weather day = getItem(position);// Instância do objeto Weather na posição do item atual.

        ViewHolder viewHolder; // para o holder dos elementos do item

        if(convertView == null){

            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext()); // Inflador do layout do view

            //Infla o layout personalizado para o item de ListView definido list_item
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            //Incializa os elementos do view
            viewHolder.conditionImageView = (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView = (TextView) convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView = (TextView) convertView.findViewById(R.id.lowTextView);
            viewHolder.hitTextView = (TextView) convertView.findViewById(R.id.hitTextView);
            viewHolder.humidityTextView = (TextView) convertView.findViewById(R.id.humidityTextView);

            // Agrupa o elementos no view a ser retornado
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

        // Seta os elementos com os valores do objeto Weather presente na lista
        viewHolder.dayTextView.setText(context.getString(R.string.day_description,
                                        day.dayOfWeek, day.description));

        viewHolder.lowTextView.setText(context.getString(R.string.low_temp, day.minTemp));

        viewHolder.hitTextView.setText(context.getString(R.string.high_temp, day.maxTemp));

        viewHolder.humidityTextView.setText(context.getString(R.string.humidity, day.humidity));


        // Retorna o view personalizado para o ListView o exibir
        return convertView;

    }

    // AsyncTask para o dowload da imagem que representa as condições climáticas do dia
    // a imagem não baixada na conexão, pois o padrão de retorno é JSON, ou seja, tipo texto
    // por isso deve ser baixada a parte.
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
