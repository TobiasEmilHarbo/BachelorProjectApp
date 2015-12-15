package heroes.bachelorprojectapp.rest;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by nicklasjust on 18/11/15.
 */
public class PostRequestTask extends AsyncTask<String, String, String> {

    private List<NameValuePair> postParameters = null;

    public void setPostParameters(List<NameValuePair> postParameters)
    {
        this.postParameters = postParameters;
    }

    @Override
    protected String doInBackground(String... params)
    {
        BufferedReader inBuffer = null;
        String result = "fail";

        try
        {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost request = new HttpPost(params[0]);

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
                    postParameters);

            request.setEntity(formEntity);
            HttpResponse httpResponse = httpClient.execute(request);

            HttpEntity responseEntity = httpResponse.getEntity();

            if(responseEntity != null)
            {
                result = EntityUtils.toString(responseEntity);
            }

        }
        catch(Exception e)
        {
            // Do something about exceptions
            result = e.getMessage();
        }
        finally
        {
            if (inBuffer != null)
            {
                try
                {
                    inBuffer.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return  result;
    }

    @Override
    protected void onPostExecute(String result) {

    }
}
