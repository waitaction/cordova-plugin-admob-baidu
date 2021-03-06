package cn.liangyongxiong.cordova.plugin.admob.baidu;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;


/**
 * Created by shion on 2017/12/15.
 */

public class BaiduAdMobBannerFragment extends DialogFragment {
    public static final String APPID = "APPID";//应用id
    public static final String BannerPosID = "BannerPosID";
    private String appId = "";//应用id
    private String bannerPosId = "";

    private Context mContext;
    private AdView adView;

    public static BaiduAdMobBannerFragment newInstance(String appid, String bannerPosID) {
        BaiduAdMobBannerFragment fragment = new BaiduAdMobBannerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(APPID, appid);
        bundle.putString(BannerPosID, bannerPosID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        appId = getArguments().getString(APPID);
        bannerPosId = getArguments().getString(BannerPosID);
        AdView.setAppSid(mContext, appId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
        }
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
                    return true;
                }
                return false;
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", "onClose");
            sendUpdate(obj, false);
        } catch (Exception e) {
        }

        doCloseBanner();
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout yourOriginnalLayout = new RelativeLayout(mContext);

        // 创建广告View
        String adPlaceId = bannerPosId; //  重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
        adView = new AdView(mContext, adPlaceId);

        // 设置监听器
        adView.setListener(new AdViewListener() {
            @Override
            public void onAdSwitch() {
            }

            @Override
            public void onAdShow(JSONObject info) {
                // 广告已经渲染出来
            }

            @Override
            public void onAdReady(AdView adView) {
                // 资源已经缓存完毕，还没有渲染出来
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "onSuccess");
                    sendUpdate(obj, true);
                } catch (Exception e) {
                }
            }

            @Override
            public void onAdFailed(String reason) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "onError");
                    obj.put("msg", reason);
                    sendUpdate(obj, false);
                } catch (Exception e) {
                }
            }

            @Override
            public void onAdClick(JSONObject info) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "onClick");
                    sendUpdate(obj, true);
                } catch (Exception e) {
                }
            }

            @Override
            public void onAdClose(JSONObject info) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "onClose");
                    sendUpdate(obj, false);
                } catch (Exception e) {
                }
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        int winW = dm.widthPixels;
        int winH = dm.heightPixels;
        int width = Math.min(winW, winH);
        int height = width * 3 / 20;

        // 将adView添加到父控件中(注：该父控件不一定为您的根控件，只要该控件能通过addView能添加广告视图即可)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        yourOriginnalLayout.addView(adView, params);
        return yourOriginnalLayout;
    }

    private void doCloseBanner() {
        if (adView != null) {
            adView.destroy();
        }
    }

    public CallbackContext callbackContext;

    public void setCallbackContext(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    private void sendUpdate(JSONObject obj, boolean keepCallback) {
        sendUpdate(obj, keepCallback, PluginResult.Status.OK);
    }

    private void sendUpdate(JSONObject obj, boolean keepCallback, PluginResult.Status status) {
        PluginResult result = new PluginResult(status, obj);
        result.setKeepCallback(keepCallback);
        callbackContext.sendPluginResult(result);
    }
}

