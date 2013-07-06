package ru.rutube.RutubeAPI;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * Created by tumbler on 16.06.13.
 */
public class HttpTransport {
    // All this only for cookie support in Volley
    // TODO: remove after transfer to DRF-2.3

    private static DefaultHttpClient sHttpClient;
    public static synchronized DefaultHttpClient getHttpClient() {
        if (sHttpClient == null)
        {
            HttpParams params = new BasicHttpParams();

            ConnManagerParams.setMaxTotalConnections(params, 100);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

            ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

            sHttpClient = new DefaultHttpClient(cm, params);
        }
        return sHttpClient;
    }
}
