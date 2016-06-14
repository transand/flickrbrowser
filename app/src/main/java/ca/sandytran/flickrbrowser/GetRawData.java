package ca.sandytran.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Sandy on 2016-06-14.
 */

enum DownloadStatus { IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK }
public class GetRawData {
    /*Just downloading raw JSON data that will be processed elsewhere*/

    private String LOG_TAG = GetRawData.class.getSimpleName();
    private String mRawUrl; //raw url start with lower case m if private variable in class
    private String mData;    // data
    private DownloadStatus mDownloadStatus;

    public GetRawData(String mRawUrl) {
        this.mRawUrl = mRawUrl;
        this.mDownloadStatus = DownloadStatus.IDLE;
    }

    public void reset(){
        this.mDownloadStatus = DownloadStatus.IDLE;
        this.mRawUrl = null;
        this.mData = null;
    }

    public String getmData() {
        return mData;
    }

    public void execute(){
        this.mDownloadStatus = DownloadStatus.PROCESSING;
        DownloadRawData downloadRawData = new DownloadRawData();
        downloadRawData.execute(mRawUrl);
    }

    public DownloadStatus getmDownloadStatus() {
        return mDownloadStatus;
    }

    public class DownloadRawData extends AsyncTask<String, Void, String>{
        protected void onPostExecute(String webData){
            //what happens after downloading
            mData = webData;
            Log.v(LOG_TAG, "Data returned was: " + mData);

            if (mData == null){
                if(mRawUrl == null){
                    mDownloadStatus = DownloadStatus.NOT_INITIALISED;
                } else {
                    mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
                }
            } else {
                //sucesss
                mDownloadStatus = DownloadStatus.OK;
            }
        }

        protected String doInBackground(String... params){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            if (params == null){
                return null;
            }

            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null){
                    //nothing to process so just return
                    return null;
                }
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while( (line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                return buffer.toString();
            } catch(IOException e){
                Log.e(LOG_TAG, "Error", e);
                return null;
            } finally {
                //executed every time
                //close connection

                if (urlConnection != null){
                    urlConnection.disconnect();
                }

                if (reader != null){
                    try{
                        reader.close();
                    } catch(final IOException e) {
                        Log.e(LOG_TAG, "error closing stream");
                    }
                }

            }
        }

    }
}
