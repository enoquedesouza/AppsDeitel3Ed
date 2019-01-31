package deitel.exemplos.weatherviewer;


import android.support.design.widget.Snackbar;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Enoque on 29/01/2019.
 *
 * A classe a Weather representa as condições climáticas de um dia específico. Essas informações serão setadas com
 * os dados recuperados do objeto JSON enviado pelo openweather.org web service.
 *
 *
 */

 class Weather {

      public final String dayOfWeek;
      public final String minTemp;
      public final String maxTemp;
      public final String humidity;
      public final String description;
      public final String iconUrl;


      /*
      * @param timeStamp é o número de segundos transcorridos desde 1º de janeiro de 1970 até o instante de timeStamp
      * @param minTemp temperatura mínima do dia passada pelo web service
      * @param maxTemp temperatura máxima do dia passada pelo web service
      * @param humidity nível de humidade do dia
      * @param description é a descrição das condições climáticas
      * @param iconName é nome da imagem que representa as condições climáticas
      */
      public Weather(long timeStamp, double minTemp, double maxTemp, int humidity,
                     String description, String iconName){

       NumberFormat numberFormat = NumberFormat.getNumberInstance(); //Recupera uma instância da classe para formatação
                                                                     //de valores numéricos
       numberFormat.setMaximumFractionDigits(0); //Casas decimais 0

       this.dayOfWeek = convertTimeStampToDay(timeStamp); // Converte o timeStamp para uma data atual, representando o dia
                                                         // o dia da semana correspondente.

       this.minTemp = numberFormat.format(minTemp) + "\u00B0F"; //string representando a temperatura mínima no dia
                                                                //"\u00B0F" representa o unicode para o simbolo °F
                                                                // de Farenheit
       this.maxTemp = numberFormat.format(maxTemp) + "\u00B0F";

       this.humidity = NumberFormat.getPercentInstance().format(humidity/100); // Recupera a representação em procentegem
                                                                               // para um vlaor numérico passado, no caso,
                                                                               // a umidade
       this.description = description;// seta a descrição

       this.iconUrl = "http://openweathermap.org/img/w/" + iconName + ".png"; // Url para o ícone que representa as condições
                                                                              //climáticas

      }


      /*
      * Este método converte o timeStamp para o valor do dia atual retornando a data formatada correspondente
      * */
      private static String convertTimeStampToDay(long timeStamp){

        Calendar calendar = Calendar.getInstance(); // recupera uma instância de Calendar para poder formatar a data

        calendar.setTimeInMillis(timeStamp*1000);// seta o tempo a ser repsentado em milisegundos. É numero de milisegundos
                                                 // transcorridos até o instante de timeStamp desde 1º e3 janeiro de 1970

        TimeZone timeZone = TimeZone.getDefault(); //Instância do fuso horário

        calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis())); // Configura a data de acordo com o fuso
                                                                                            // horário do dispositivo

        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE"); //Formata data de forma a representar somente o nome do dia


        return dateFormatter.format(calendar.getTime()); // formata a data no padrão especificado em SimpleDateFromat e retorna
                                                         // o nome do dia

      }



}
