package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.net.*;
import android.os.*;
import android.util.DisplayMetrics;
import android.widget.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

final class XpSystemInformation extends XpToolPanel {
    private TextView report;private String section="System Summary";
    XpSystemInformation(Context c,DesktopStore store,Host host){super(c,store,host,"@msinfo");command("Refresh",this::refresh);command("Copy",()->copy("System information",report(c)));command("Save Report…",()->host.export(id,"System-Information.txt","text/plain",report(c).getBytes(StandardCharsets.UTF_8)));
        String[] pages={"System Summary","Memory and Storage","Display","Network","Hardware"};Spinner selector=new Spinner(c);selector.setContentDescription("System Information category");selector.setAdapter(new ArrayAdapter<>(c,android.R.layout.simple_spinner_dropdown_item,pages));toolbar.addView(selector);report=text("",12);report.setTextIsSelectable(true);report.setGravity(android.view.Gravity.TOP|android.view.Gravity.START);report.setPadding(dp(8),dp(8),dp(8),dp(8));report.setTypeface(android.graphics.Typeface.MONOSPACE);scroll(report);selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,android.view.View v,int n,long id){section=pages[n];refresh();}});refresh();
    }
    private void refresh(){report.setText(sections(getContext()).get(section));say("Current Android values • "+section);}
    static LinkedHashMap<String,String> sections(Context c){LinkedHashMap<String,String> out=new LinkedHashMap<>();
        out.put("System Summary","OS Name: Android "+Build.VERSION.RELEASE+"\nSDK: "+Build.VERSION.SDK_INT+"\nSecurity patch: "+Build.VERSION.SECURITY_PATCH+"\nManufacturer: "+Build.MANUFACTURER+"\nModel: "+Build.MODEL+"\nProduct: "+Build.PRODUCT+"\nArchitectures: "+String.join(", ",Build.SUPPORTED_ABIS)+"\nAvailable processors: "+Runtime.getRuntime().availableProcessors()+"\nNth Desktop XP: "+AppInfo.version(c)+"\nLocale: "+Locale.getDefault().toLanguageTag()+"\nTime zone: "+TimeZone.getDefault().getID()+"\nUptime: "+SystemClock.elapsedRealtime()/1000+" seconds");
        ActivityManager.MemoryInfo memory=new ActivityManager.MemoryInfo();ActivityManager am=c.getSystemService(ActivityManager.class);if(am!=null)am.getMemoryInfo(memory);StatFs disk=new StatFs(c.getFilesDir().getAbsolutePath());out.put("Memory and Storage","Physical memory: "+memory.totalMem+" bytes\nAvailable physical memory: "+memory.availMem+" bytes\nLow-memory state: "+memory.lowMemory+"\nApp Java heap used: "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())+" bytes\nApp Java heap maximum: "+Runtime.getRuntime().maxMemory()+" bytes\nInternal volume total: "+disk.getTotalBytes()+" bytes\nInternal volume available: "+disk.getAvailableBytes()+" bytes");
        DisplayMetrics m=c.getResources().getDisplayMetrics();out.put("Display","Current surface: "+m.widthPixels+" × "+m.heightPixels+" pixels\nDensity: "+m.densityDpi+" dpi\nFont scale: "+c.getResources().getConfiguration().fontScale+"\nOrientation: "+(c.getResources().getConfiguration().orientation==2?"Landscape":"Portrait"));
        String network="No active connection";try{ConnectivityManager manager=c.getSystemService(ConnectivityManager.class);NetworkCapabilities caps=manager==null?null:manager.getNetworkCapabilities(manager.getActiveNetwork());if(caps!=null)network="Connected: "+(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Cellular":caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"Ethernet":"Other network")+"\nInternet validated: "+caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)+"\nVPN transport: "+caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);}catch(RuntimeException ignored){}out.put("Network",network);
        StringBuilder hardware=new StringBuilder("Android-reported sensors\n\n");SensorManager sm=c.getSystemService(SensorManager.class);if(sm!=null)for(Sensor s:sm.getSensorList(Sensor.TYPE_ALL))hardware.append(s.getName()).append(" — ").append(s.getVendor()).append('\n');if(hardware.length()<40)hardware.append("No sensors reported.\n");out.put("Hardware",hardware.toString());return out;
    }
    static String report(Context c){StringBuilder s=new StringBuilder("Nth Desktop XP — System Information\n\n");sections(c).forEach((k,v)->s.append(k).append('\n').append(v).append("\n\n"));return s.toString();}
}
