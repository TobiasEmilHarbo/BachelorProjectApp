package heroes.bachelorprojectapp.rest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tobias on 07-12-2015.
 */
public class RestResponse {

    String data;

    public RestResponse(String d)
    {
        data = d;
    }

    public String asString()
    {
        return data;
    }

    public JSONObject asJSON() throws JSONException
    {
        return new JSONObject(data);
    }

    public JSONObject getJSON(String name) throws JSONException
    {
        return (JSONObject) (new JSONObject(data)).get(name);
    }

    public Boolean getBoolean(String name) throws JSONException
    {
        return (Boolean) (new JSONObject(data)).get(name);
    }

    public String getString(String name) throws JSONException
    {
        return (String) (new JSONObject(data)).get(name);
    }

    public int getInt(String name) throws JSONException
    {
        return (int) (new JSONObject(data)).get(name);
    }
}
