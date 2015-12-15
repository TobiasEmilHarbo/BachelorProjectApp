package heroes.bachelorprojectapp.rest;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nicklasjust on 18/11/15.
 */
public class GetRequestTask extends AsyncTask<String, String, String>
{
    @Override
    protected String doInBackground(String... params)
    {
        StringBuilder data = new StringBuilder();

        try
        {
            URL url = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null)
            {
                data.append(line);
            }
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }

        return data.toString();
    }
}
