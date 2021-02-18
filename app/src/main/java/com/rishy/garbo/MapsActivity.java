package com.rishy.garbo;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.MobileAds;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;

import com.example.owner.garbo.BuildConfig;
import com.example.owner.garbo.R;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;


import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    Button nav, newdust;
    private GoogleMap mMap;
    final static private int NEW_PICTURE = 1;
    private String mCameraFileName;
    public String mainOutPath;
    public String emailBody;
    public Uri Mouturi;
    ArrayList<ListItem> pins = new ArrayList<ListItem>();
    double min = 1000000.00;
    double dist = 0.0;
    double tempdist = 0.0;
    int index = 0;
    int CHECK_UPDATE = 1;





    private InterstitialAd mInterstitialAd;
    int clickNumber = 0;


    public MapsActivity() throws ParseException {
    }


    public class ListItem {
        private String _title;
        private double _lat;
        private double _lng;

        public ListItem(String title, double lat, double lng) {
            _title = title;
            _lat = lat;
            _lng = lng;
        }
    }



    protected void onResume()
    {
        super.onResume();
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.updateyourapp, null);
        TextView mConfirmation = (TextView) mView.findViewById(R.id.Confirmation2);
        TextView mSure = (TextView) mView.findViewById(R.id.sure2);
        Button mConfirm = (Button) mView.findViewById(R.id.confirm2);
        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();


        Calendar expirationDate = Calendar.getInstance();
        expirationDate.set(2018, 7, 15);  //hardcoded expiration date
        Calendar t = Calendar.getInstance();  //Calendar with current time/date
        if (t.compareTo(expirationDate) == 1){
            dialog.show();
            mConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url ="market://details?id=" + getPackageName();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
            }
        });
            CHECK_UPDATE = 0;
        }



    }











    public class distanceFinder
    {
        private double _lat1;
        private double _lng1;
        private double _lat2;
        private double _lng2;
        private int Radius=6371;




        public distanceFinder(double lat1,double lng1, double lat2, double lng2)
        {

            _lat1 = lat1;
            _lng1 = lng1;
            _lat2 = lat2;
            _lng2 = lng2;

            double dLat = Math.toRadians(lat2-lat1);
            double dLon = Math.toRadians(lng2-lng1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLon/2) * Math.sin(dLon/2);
            double c = 2 * Math.asin(Math.sqrt(a));
            double valueResult= Radius*c;
            double km=valueResult/1;
            DecimalFormat newFormat = new DecimalFormat("####");
            int kmInDec =  Integer.valueOf(newFormat.format(km));
            double meter=valueResult%1000;
            int  meterInDec= Integer.valueOf(newFormat.format(meter));
            Log.i("Radius Value",""+valueResult+"   KM  "+kmInDec+" Meter   "+meterInDec);
             dist = Radius * c;




        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationManager locationManagerNetwork = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final Location locationBlue = locationManagerNetwork.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        onResume();
        MobileAds.initialize(this,"ca-app-pub-4308858485174824~4975018957");
        if(CHECK_UPDATE == 1) {
            if(locationBlue != null) {



                final double latfinal = locationBlue.getLatitude();
                final double lngfinal = locationBlue.getLongitude();
                setContentView(R.layout.activity_maps);
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                ImageButton rateus = (ImageButton) findViewById(R.id.rateus);
                ImageButton privpol = (ImageButton) findViewById(R.id.privpol);
                rateus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("market://details?id=" + getPackageName()));
                        startActivity(i);
                    }
                });
                privpol.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://docs.google.com/document/d/e/2PACX-1vQmdH5LB7Ba6PFd_n_63Pe6YMmc6SrziwwOUVoy-YtzddN2VMbE_qlLB5bxESVOwPu43kUXWTYQIXMx/pub"));
                        startActivity(i);
                    }
                });
                toolbar.setClickable(true);
                toolbar.setNavigationIcon(R.drawable.ic_drawerlines);
                toolbar.setNavigationOnClickListener((new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.openDrawer(GravityCompat.START);

                    }
                }));


                nav = (Button) findViewById(R.id.nav);
                newdust = (Button) findViewById(R.id.newdust);


                mInterstitialAd = new InterstitialAd(this);
                mInterstitialAd.setAdUnitId("ca-app-pub-4308858485174824/8956843608");
                mInterstitialAd.loadAd(new AdRequest.Builder().build());


//
//            int PERMISSION_ALL = 1;
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.CAMERA,
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_ALL);


//            LocationManager locationManagerNetwork = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            final Location locationBlue = locationManagerNetwork.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            final double latfinal = locationBlue.getLatitude();
//            final double lngfinal = locationBlue.getLongitude();


                //////////////////////////////////////////////////////
                //////////////////////////////////////////////////////
                //////////////////////////////////////////////////////
                //////////////////////////////////////////////////////
                //pins get added here
                //////////////////////////////////////////////////////
                //////////////////////////////////////////////////////
                //////////////////////////////////////////////////////






                pins.add(new ListItem(" Dustbin near Seaface House ", 18.9942499, 72.8142577));
                pins.add(new ListItem(" Dustbin near Geeta Store ", 18.9951541, 72.8148216));
                pins.add(new ListItem("B.G Kher Marg Dustbin 1", 18.9968562, 72.8156827));
                pins.add(new ListItem("B.G Kher Marg Dustbin 2", 18.9985731, 72.8170191));
                pins.add(new ListItem("Dustbin Near Neelkanth Business Park", 19.0800874, 72.8951823));
                pins.add(new ListItem("Dustbin Near Mohammad Estate Bus Stop", 19.0737217, 72.8711766));
                pins.add(new ListItem("Dustbin Near Semsons International Gate 2", 19.0666551, 72.8657087));
                pins.add(new ListItem("Dustbin Near Diamond Market Bus Stop", 19.067127, 72.864854));
                pins.add(new ListItem("Dustbin Near ICICI Junction", 19.070081, 72.855979));
                pins.add(new ListItem("Dustbin Near Tata Colony Bus Stop", 19.063881, 72.866317));
                pins.add(new ListItem("Dustbin Near Income Tax Office Bus Stop", 19.061790, 72.852610));
                pins.add(new ListItem("Dustbin Near Reserve Bank Of India", 19.057618, 72.85376));
                pins.add(new ListItem("Dustbin Near Sir Ratan Tata Institute Annex", 18.9592277, 72.810004));
                pins.add(new ListItem("Dustbin near White House Building", 18.9703228, 72.8096347));
                pins.add(new ListItem("Dustbin near Balbhavan Bus Stop", 18.951263, 72.817402));
                pins.add(new ListItem("Dustbin near Axis Bank ATM Charni Road", 18.951541, 72.818843));
                pins.add(new ListItem("Dustbin near Axis Bank ATM Marine Lines", 18.949056, 72.820411));
                pins.add(new ListItem("Dustbin Near Nirmal House", 18.9703228, 72.8096347));
                pins.add(new ListItem("Dustbin near Charni Road Train Station", 18.951899, 72.817974));
                pins.add(new ListItem("Dustbin near Charni Road Train Station 2", 18.9536857, 72.8159129));
                pins.add(new ListItem("Dustbin near Birla Krida Kendra", 18.954791, 72.814631));
                pins.add(new ListItem("Dustbin near Wilson College", 18.956182, 72.810962));
                pins.add(new ListItem("Dustbin near Wilson College 2", 18.9561018, 72.811112));
                pins.add(new ListItem("Dustbin near Indira Kunj", 18.960305, 72.810004));
                pins.add(new ListItem("Dustbin near Nalanda Bus Stop", 18.9669158, 72.8077882));
                pins.add(new ListItem("Dustbin near Kamani House", 18.974272, 72.809635));
                pins.add(new ListItem("Dustbin near Worli Weekly Farmer's Market", 19.002412, 72.814372));
                pins.add(new ListItem("Dustbin near Cafe Coffee Day", 19.001990, 72.814476));
                pins.add(new ListItem("Dustbin near ICICI Bank Hughes Rd", 18.959144, 72.809999));
                pins.add(new ListItem("Dustbin near sonal stores", 19.004186, 72.813594));
                pins.add(new ListItem("Dustbin near worli seaface S ", 19.005302, 72.813819));
                pins.add(new ListItem("Dustbin near worli seaface N ", 19.014131, 72.816998));
                pins.add(new ListItem("Dustbin near national association of blind", 19.010962, 72.816201));
                pins.add(new ListItem("Dustbin near worli police station", 19.008245, 72.814679));
                pins.add(new ListItem("Dustbin near watumall college", 19.002033, 72.811958));
                pins.add(new ListItem("Dustbin near mafco farm fair ", 18.998823, 72.811669));
                pins.add(new ListItem("Dustbin near aarey sarita ", 18.998434, 72.811717));
                pins.add(new ListItem("Dustbin near milk dairy", 18.997691, 72.811852));
                pins.add(new ListItem("Dustbin near le quinta designer studio", 18.9943479, 72.8499287));
                pins.add(new ListItem("Dustbin near watumall college", 19.002033, 72.811958));
                pins.add(new ListItem("Dustbin near mafco farm fair ", 18.998823, 72.811669));
                pins.add(new ListItem("Dustbin near aarey sarita ", 18.998434, 72.811717));
                pins.add(new ListItem("Dustbin near le quinta designer studio", 18.9943479, 72.8499287));
                pins.add(new ListItem("Dustbin near ankit medico", 19.0188786, 72.8671542));
                pins.add(new ListItem("Dustbin near hitech mobiles ", 19.017989, 72.870123));
                pins.add(new ListItem("Dustbin near antop hill ", 19.021843, 72.866143));
                pins.add(new ListItem("Dustbin near vashi flyover", 19.0675385, 73.0068467));
                pins.add(new ListItem("Dustbin near mcdonalds", 19.0215162, 73.1026346));
                pins.add(new ListItem("Dustbin near janta steel", 18.994511, 72.849559));
                pins.add(new ListItem("Dustbin near maxtone electronics ", 18.9888313, 72.8308964));
                pins.add(new ListItem("Dustbin near lower parel monorail station ", 18.9932906, 72.8315375));
                pins.add(new ListItem("Dustbin near minerva rd", 18.9888026, 72.8306295));
                pins.add(new ListItem("Dustbin near kotak mahindra bank", 18.9599769, 72.8095492));
                pins.add(new ListItem("Dustbin near lodha primero ", 18.987367, 72.829369));
                pins.add(new ListItem("Dustbin near om shree ganesh mobile store", 18.9985103, 72.8220994));
                pins.add(new ListItem("Dustbin near mafco farm fair 2 ", 18.9985656, 72.8132092));
                pins.add(new ListItem("Dustbin near khedshivapur toll naka ", 18.327479, 73.8522254));
                pins.add(new ListItem("Dustbin near dominos shivkhedapur ", 18.371941, 73.8542573));
                pins.add(new ListItem("Dustbin near Thomson Wine Shop Maxem", 14.946452, 74.069255));
                pins.add(new ListItem("Dustbin near MalikaArjun Temple Canacona", 15.022701, 74.076241));
                pins.add(new ListItem("Dustbin near Castaad Kitchen", 14.948430, 74.057606));
                pins.add(new ListItem("Dustbin near Cuncolim Bus Stand", 15.182360, 73.997519));
                pins.add(new ListItem("Dustbin near BP Petrol Pump Cuncolim", 15.178566, 73.998832));
                pins.add(new ListItem("Dustbin near Indian Oil Cuncolim", 15.196715, 73.996648));
                pins.add(new ListItem("Dustbin near Holy Cross Chapel Cuncolim", 15.177520, 74.012795));
                pins.add(new ListItem("Dustbin near Valley View Coffee", 15.162972, 74.012194));
                pins.add(new ListItem("Dustbin near Shree Ganpati Devstaan", 15.049552, 74.023561));
                pins.add(new ListItem("Dustbin near Shree Krishna General Store", 14.969607, 74.087371));
                pins.add(new ListItem("Dustbin near HP Petrol Pump near paradise hotel karwar ", 14.916841, 74.080762));
                pins.add(new ListItem("Dustbin near Sonarwada Karwar",14.8169854 ,74.148035 ));
                pins.add(new ListItem("Dustbin near KTC Bus Stand Assanora", 15.619299, 73.901382));
                pins.add(new ListItem("Dustbin near Sirsi Urban Bank", 14.828577, 74.137929));
                pins.add(new ListItem("Dustbin near Canteen Colvale", 15.618933, 73.822925));
                pins.add(new ListItem("Dustbin near Citrus Hotel Karwar", 14.808891, 74.133776));
                pins.add(new ListItem("Dustbin near Prabodhankar Thackeray Chowk", 19.019787, 72.836452));
                pins.add(new ListItem("Dustbin near Yamuna Express Cafe Agra",  27.3027987 ,78.0100566));
                pins.add(new ListItem("Dustbin near Madhuvan Apartment", 19.224048 ,72.8357927));
                pins.add(new ListItem("Dustbin near Prayatna Building", 19.2241145 ,72.8340922));
                pins.add(new ListItem("Dustbin near VIva Wines", 15.265987, 73.944815));
                pins.add(new ListItem("Dustbin near Apollo Pharmacy Benaulim", 15.257184  ,73.9658687));
                pins.add(new ListItem("Dustbin near HP petrol Pump Canacona", 15.029386, 74.0330073));
                pins.add(new ListItem("Dustbin near Streamline MiniMart", 15.011551,74.0350008));
                pins.add(new ListItem("Dustbin near Corporation Bank with ATM Canacona", 15.0070673 ,74.04822));
                pins.add(new ListItem("Dustbin near Taj East Gate" ,27.1662224 ,78.0536361));
                pins.add(new ListItem("Dustbin near Taj Mahal Great Gate", 27.172110, 78.042145));
                pins.add(new ListItem("Dustbin near Shivaji Park", 19.0262066,72.8377348));
                pins.add(new ListItem("Dustbin near Pune Bengalaru Exp", 17.822374, 73.964453));
                pins.add(new ListItem("Dustbin near Makxpresso Espresso Store", 19.002133, 72.814181));
                pins.add(new ListItem("Dustbin near Shri Ganesh Stores", 19.0164659  ,72.827142));
                pins.add(new ListItem("Dustbin near Babasaheb Worlikar Chowk Bus Stop", 19.0127718 , 72.8254463));
                pins.add(new ListItem("Dustbin near Shri Nilkantheshwar Mandir", 19.0004794 ,72.8162088));
                pins.add(new ListItem("Dustbin near Panchkutir/Ganesh Mandir", 15.0070673 ,74.04822));
                pins.add(new ListItem("Dustbin near 38 Degree East", 19.122507, 72.908469));
                pins.add(new ListItem("Dustbin near Jawahar Nagar Bus Stop", 19.159975, 72.845900));
                pins.add(new ListItem("Dustbin near Dadar Train Station", 19.019023, 72.843118));
                pins.add(new ListItem("Dustbin near Sewri Train Station", 18.998672, 72.854513));
                pins.add(new ListItem("Dustbin near Bhayandar Station", 19.311488, 72.852649));
                pins.add(new ListItem("Dustbin near Kaman Rd Train Station", 19.337828, 72.918422));
                pins.add(new ListItem("Dustbin near Malad Station", 19.188481, 72.848855));
                pins.add(new ListItem("Dustbin near Goregaon Station", 19.164849, 72.849252));
                pins.add(new ListItem("Dustbin near Pay and Use Public Toilet,Chembur", 19.062129, 72.899405));
                pins.add(new ListItem("Dustbin near BMC Toilet Kurla", 19.060089, 72.872490));
                pins.add(new ListItem("Dustbin near seaface 1 ",19.0022028,72.8121432));
                pins.add(new ListItem("Dustbin near seaface 2 ",19.0023642,72.8124126));
                pins.add(new ListItem("Dustbin near seaface 3 ",19.0038866,72.8125032));
                pins.add(new ListItem("Dustbin near seaface 4 ",19.0044837,72.813849));
                pins.add(new ListItem("Dustbin near seaface 5 ",19.0052375,72.8137737));
                pins.add(new ListItem("Dustbin near seaface 6 ",19.0053109,72.8137887));
                pins.add(new ListItem("Dustbin near seaface 7 ",19.0057403,72.8140426));
                pins.add(new ListItem("Dustbin near seaface 8 ",19.006344,72.8140349));
                pins.add(new ListItem("Dustbin near seaface 9 ",19.0066766,72.8142231));
                pins.add(new ListItem("Dustbin near seaface 10 ",19.0068797,72.814482));
                pins.add(new ListItem("Dustbin near seaface 11 ",19.0079001,72.8144568));
                pins.add(new ListItem("Dustbin near seaface 12 ",19.0082236,72.8145942));
                pins.add(new ListItem("Dustbin near seaface 13 ",19.0085864,72.8147075));
                pins.add(new ListItem("Dustbin near seaface 14",19.0085864,72.8147075));
                pins.add(new ListItem("Dustbin near seaface 15",19.0087342,72.8147897));
                pins.add(new ListItem("Dustbin near seaface 16",19.0092002,72.8149405));
                pins.add(new ListItem("Dustbin near seaface 17",19.0096829,72.8154769));
                pins.add(new ListItem("Dustbin near seaface 18",19.0102634,72.8159273));
                pins.add(new ListItem("Dustbin near seaface 19",19.0111031,72.8163393));
                pins.add(new ListItem("Dustbin near seaface 20",19.0120373,72.8168457));
                pins.add(new ListItem("Dustbin near Jia Art Jewellery",19.209072  , 72.8728347));
                pins.add(new ListItem("Dustbin near Jagannath Shankar Seth Rd ",18.9507714 ,72.8236414));
                pins.add(new ListItem("Dustbin near Hughes Rd ",18.9588435 ,72.8099619));
                pins.add(new ListItem("Dustbin near Century Mill ",19.006458 ,72.8268971));
                pins.add(new ListItem("Dustbin near SK Bole Rd ",19.0191074 ,72.8367747));
                pins.add(new ListItem("Dustbin near OSHo Glimpse Mumbai ",18.9972724 ,72.8166808));
                pins.add(new ListItem("Dustbin near Hari Glass Traders ",18.9984747 ,72.8231577));
                pins.add(new ListItem("Dustbin near 4 Dr Moses Rd ",18.9981514 ,72.8178083));
                pins.add(new ListItem("Dustbin near Sangeeta Building ",19.0017504, 72.8154853));
                pins.add(new ListItem("Dustbin near Police Colony ",19.0043805 , 72.8167689));
                pins.add(new ListItem("Dustbin near Station Rd ",19.1648595 ,72.8505303));
                pins.add(new ListItem("Dustbin near Don Bosco School Building ",19.2304001 , 72.841715));
                pins.add(new ListItem("Dustbin near Amogh Apartments ",19.1129719 ,72.828173));
                pins.add(new ListItem("Dustbin near Hush Puppies ",19.2092029 ,72.9721285));
                pins.add(new ListItem("Dustbin near RamNarayan Narkar Marg ",19.0792795  , 72.9095361));
                pins.add(new ListItem("Dustbin near Worli Seaface 21 ",18.996612 ,72.8129651));
                pins.add(new ListItem("Dustbin near Worli Seaface 22 ",18.9972364 ,72.8128637));
                pins.add(new ListItem("Dustbin near Worli Seaface 23 ",18.997224, 72.811797));
                pins.add(new ListItem("Dustbin near Worli Seaface 24 ",18.9983881 ,72.8121192));
                pins.add(new ListItem("Dustbin near Worli Seaface 25 ",18.99871 ,72.8119473));
                pins.add(new ListItem("Dustbin near Worli Seaface 26 ",19.000071  , 72.8123064));
                pins.add(new ListItem("Dustbin near Worli Seaface 27 ",18.9997464 , 72.811739));
                pins.add(new ListItem("Dustbin near Worli Seaface 28 ",18.999936 ,72.8117413));
                pins.add(new ListItem("Dustbin near Worli Seaface 29 ",19.001122, 72.8120906));
                pins.add(new ListItem("Dustbin near Worli Seaface 30 ",18.998292, 72.811393));
                pins.add(new ListItem("Dustbin near Jarimari Darshan ",18.997709, 72.814436));
                pins.add(new ListItem("Dustbin near Desai Oceanic ",19.0166057,72.8194649));
                pins.add(new ListItem("Dustbin near Worli Seaface 31 ",19.0131784,72.8168193));
                pins.add(new ListItem("Dustbin near Worli Seaface 32 ",19.0134663,72.816886));

























                Log.e("world", String.valueOf(pins.size()));

                /////////////////////////////////////////////////////
                ////////////////////////////////////////////////
                ///////////////////////////////////////////////
                ////////////////////////////////////////////////////////


                nav.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View view) {


                        for (int i = 0; i < pins.size(); i++) {


                            distanceFinder temp;
                            temp = new distanceFinder(pins.get(i)._lat, pins.get(i)._lng, latfinal, lngfinal);
                            if (dist < min) {
                                min = dist;
                                tempdist = dist;
                                index = i;
                                Log.e("asas", "nearest pin" + i);
                            }

                        }
                        for (int i = 0; i < pins.size(); i++) {


                            if (index == i) {
                                final Intent j = new Intent(android.content.Intent.ACTION_VIEW,
                                        Uri.parse("google.navigation:q=" + pins.get(i)._lat + "," + pins.get(i)._lng + "&mode=w&avoid=f"));
                                        Log.e("mindist", String.valueOf(tempdist));
                                if(tempdist<0.75){
                                    AlertDialog.Builder sBuilder = new AlertDialog.Builder(MapsActivity.this);
                                    View sView = getLayoutInflater().inflate(R.layout.results, null);
                                    TextView sConfirmation = (TextView) sView.findViewById(R.id.Confirmationreal);
                                    ImageView sdustbinpic = (ImageView) sView.findViewById(R.id.dustbinpic);
                                    TextView sdustbinname = (TextView) sView.findViewById(R.id.dustbinname2);
                                    Button srealnav = (Button) sView.findViewById(R.id.realnav2);
                                    sdustbinname.setText(pins.get(i)._title);
                                    sBuilder.setView(sView);
                                    final AlertDialog sdialog = sBuilder.create();
                                    sdialog.show();
                                    srealnav.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(j);
                                        }
                                    });

                                }
                                else{
                                    AlertDialog.Builder rBuilder = new AlertDialog.Builder(MapsActivity.this);
                                    View rView = getLayoutInflater().inflate(R.layout.nodustnear, null);
                                    TextView rConfirmation = (TextView) rView.findViewById(R.id.Confirmation3);
                                    TextView rSure = (TextView) rView.findViewById(R.id.sure3);
                                    Button rConfirm = (Button) rView.findViewById(R.id.navigate2);
                                    Button rdust = (Button) rView.findViewById(R.id.adddust2);
                                    Button rgoback = (Button) rView.findViewById(R.id.goback2);
                                    rBuilder.setView(rView);
                                    final AlertDialog rdialog = rBuilder.create();
                                    rdialog.show();

                                    rgoback.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            rdialog.dismiss();
                                        }
                                    });
                                    final int finalI = i;
                                    rConfirm.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder sBuilder = new AlertDialog.Builder(MapsActivity.this);
                                            View sView = getLayoutInflater().inflate(R.layout.results, null);
                                            TextView sConfirmation = (TextView) sView.findViewById(R.id.Confirmationreal);
                                            ImageView sdustbinpic = (ImageView) sView.findViewById(R.id.dustbinpic);
                                            TextView sdustbinname = (TextView) sView.findViewById(R.id.dustbinname2);
                                            Button srealnav = (Button) sView.findViewById(R.id.realnav2);
                                            sdustbinname.setText(pins.get(finalI)._title);
                                            sBuilder.setView(sView);
                                            final AlertDialog sdialog = sBuilder.create();
                                            sdialog.show();
                                            srealnav.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    startActivity(j);
                                                }
                                            });
                                        }
                                    });

                                    rdust.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (clickNumber <= 2) {
                                                clickNumber++;
                                            } else {
                                                clickNumber = 0;
                                                if (mInterstitialAd.isLoaded()) {
                                                    mInterstitialAd.show();
                                                } else {
                                                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                                                }
                                                // show ad here
                                            }


                                            Intent intent = new Intent();
                                            // Picture from camera
                                            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                                            // This is not the right way to do this, but for some reason, having
                                            // it store it in
                                            // MediaStore.Images.Media.EXTERNAL_CONTENT_URI isn't working right.

                                            Date date = new Date();
                                            DateFormat df = new SimpleDateFormat("-mm-ss");

                                            String newPicFile = "Garb" + df.format(date) + ".jpg";
                                            String outPath = "/sdcard/" + newPicFile;
                                            File outFile = new File(outPath);
                                            mainOutPath = outPath;
                                            Log.e("IMGPATH", "A" + outPath);

                                            mCameraFileName = outFile.toString();

                                            if (Build.VERSION.SDK_INT >= 23) {

                                                Mouturi = FileProvider.getUriForFile(MapsActivity.this,
                                                        BuildConfig.APPLICATION_ID + ".provider",
                                                        outFile);
                                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Mouturi);

                                            } else {
                                                Uri outuri = Uri.fromFile(outFile);
                                                intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                                            }
//

                                            startActivityForResult(intent, NEW_PICTURE);


                                        }
                                    });

                                }
                            }

                        }

                    }
                });


                newdust.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View view) {


                        if (clickNumber <= 2) {
                            clickNumber++;
                        } else {
                            clickNumber = 0;
                            if (mInterstitialAd.isLoaded()) {
                                mInterstitialAd.show();
                            } else {
                                Log.d("TAG", "The interstitial wasn't loaded yet.");
                            }
                            // show ad here
                        }


                        Intent intent = new Intent();
                        // Picture from camera
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                        // This is not the right way to do this, but for some reason, having
                        // it store it in
                        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI isn't working right.

                        Date date = new Date();
                        DateFormat df = new SimpleDateFormat("-mm-ss");

                        String newPicFile = "Garb" + df.format(date) + ".jpg";
                        String outPath = "/sdcard/" + newPicFile;
                        File outFile = new File(outPath);
                        mainOutPath = outPath;
                        Log.e("IMGPATH", "A" + outPath);

                        mCameraFileName = outFile.toString();

                        if (Build.VERSION.SDK_INT >= 23) {

                            Mouturi = FileProvider.getUriForFile(MapsActivity.this,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    outFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Mouturi);

                        } else {
                            Uri outuri = Uri.fromFile(outFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                        }
//

                        startActivityForResult(intent, NEW_PICTURE);


                    }
                });

            }
            else
                {
                    AlertDialog.Builder checkBuilder = new AlertDialog.Builder(MapsActivity.this);
                    View checkView = getLayoutInflater().inflate(R.layout.checklocation, null);
                    TextView cConfirmation = (TextView)  checkView.findViewById(R.id.gpscheck);
                    TextView cSure = (TextView) checkView.findViewById(R.id.gpscheck2);
                    Button cConfirm = (Button) checkView.findViewById(R.id.okay);
                    checkBuilder.setView(checkView);
                    final AlertDialog dialog2 = checkBuilder.create();
                    dialog2.show();
                    cConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog2.dismiss();
                        }
                    });
                }
        }
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_PICTURE) {
            // return from file upload
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();

                }
                if (uri == null && mCameraFileName != null) {



                    if (Build.VERSION.SDK_INT >= 23) {
                        uri = FileProvider.getUriForFile(MapsActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                new File(mCameraFileName));
                    } else {
                        uri = Uri.fromFile(new File(mCameraFileName));

                    }



                }
                File file = new File(mCameraFileName);
                if (!file.exists()) {
                    file.mkdir();
                }
                Log.e("a", "hello");
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.sendemaildialog, null);
                TextView mConfirmation = (TextView) mView.findViewById(R.id.Confirmation);
                TextView mSure = (TextView) mView.findViewById(R.id.sure);
                Button mConfirm = (Button) mView.findViewById(R.id.confirm);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                LocationManager locationManagerNetwork = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location2 = locationManagerNetwork.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (location2 != null) {
                    String message = String
                            .format("New Bin location : \n Latitude: %1$s \n Longitude: %2$s",
                                    location2.getLatitude(), location2.getLongitude());
                    emailBody = message;

                }

                mConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("application/image");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"corporationgarbo@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "New Location Suggested");
                        i.putExtra(Intent.EXTRA_TEXT, emailBody);
                        if (Build.VERSION.SDK_INT >= 23) {
                            // Call some material design APIs here
                            i.putExtra(Intent.EXTRA_STREAM, Mouturi);
                        } else {
                            // Implement this feature without material design
                            Uri uri = Uri.parse("file://" + mainOutPath);
                            i.putExtra(Intent.EXTRA_STREAM, uri);
                        }

                        try {
                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(MapsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                });


            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

            if (!success) {
                Log.e("TAG", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("TAG", "Can't find style. Error: ");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationManager locationManagerNetwork = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final Location locationBlue = locationManagerNetwork.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng TrueLocation =new LatLng(locationBlue.getLatitude(),locationBlue.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(TrueLocation));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TrueLocation, 15));




        for(int j = 0; j<pins.size();j++)
        {
            mMap.addMarker(new MarkerOptions().position(new LatLng(pins.get(j)._lat,pins.get(j)._lng)).title(pins.get(j)._title).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dustpin)));
            Log.e("title",pins.get(j)._title);
        }








//        // Add a marker in Sydney and move the camera










    }






}











/**
 * Manipulates the map once available.
 * This callback is triggered when the map is ready to be used.
 * This is where we can add markers or lines, add listeners or move the camera. In this case,
 * we just add a marker near Sydney, Australia.
 * If Google Play services is not installed on the device, the user will be prompted to install
 * it inside the SupportMapFragment. This method will only be triggered once the user has
 * installed Google Play services and returned to the app.
 */






