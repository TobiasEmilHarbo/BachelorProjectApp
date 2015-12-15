package heroes.bachelorprojectapp.rest;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tobias on 20-11-2015.
 */
public class RestRequest {

    //private String data = null;
    private RestResponse response = null;

    public void post(String url, List<NameValuePair> parameters) throws ExecutionException, InterruptedException
    {
        PostRequestTask request = new PostRequestTask();
        request.setPostParameters(parameters);
        String data = request.execute(url).get();
        response = new RestResponse(data);
    }

    public void get(String url) throws ExecutionException, InterruptedException
    {
        GetRequestTask request = new GetRequestTask();
        String data = request.execute(url).get();
        response = new RestResponse(data);
    }

    public RestResponse getResponse()
    {
        return response;
    }
}
