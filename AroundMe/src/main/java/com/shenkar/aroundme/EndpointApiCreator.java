package com.shenkar.aroundme;

import java.util.HashMap;
import com.appspot.enhanced_cable_88320.aroundmeapi.Aroundmeapi;
import com.shenkar.aroundme.deviceinfoendpoint.Deviceinfoendpoint;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;

/*
 * This class response for the creation of the endpoints object
 * In case of new enpoint, this class must be updated.
 * 
 */
@SuppressWarnings("rawtypes")
public class EndpointApiCreator {
	private static boolean isInitialized = false;
	private static GoogleAccountCredential credential;
	// map of all the endpoints that was created.
	private static HashMap<Class, AbstractGoogleJsonClient> endpointsList;
	static {
		endpointsList = new HashMap<Class, AbstractGoogleJsonClient>();
	}

	public static void initialize(GoogleAccountCredential credential) {
		if(isInitialized) return;
		isInitialized = true;
		EndpointApiCreator.credential = credential;
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractGoogleJsonClient> T getApi(
			Class<T> endpointClass) throws Exception {

		if (!isInitialized)
			throw new Exception(
					"Endoint API creator must be intialized before using it.");
		if (endpointsList.containsKey(endpointClass))
			return (T) endpointsList.get(endpointClass);
		// The builder.
		T.Builder endpointBuilder = null;

		// check for the relevant builder by the class type.
		if (Deviceinfoendpoint.class.isAssignableFrom(endpointClass)) {
			endpointBuilder = new Deviceinfoendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new GsonFactory(),
					new HttpRequestInitializer() {
						public void initialize(HttpRequest httpRequest) {
						}
					});
		} else if (Aroundmeapi.class.isAssignableFrom(endpointClass)) {
			endpointBuilder = new Aroundmeapi.Builder(
					AndroidHttp.newCompatibleTransport(), new GsonFactory(),
					credential);
		}
		// build the endpoint and save to the map obj.
		T ret = (T) CloudEndpointUtils.updateBuilder(endpointBuilder).build();
		endpointsList.put(endpointClass, ret);
		return ret;
	}
}
