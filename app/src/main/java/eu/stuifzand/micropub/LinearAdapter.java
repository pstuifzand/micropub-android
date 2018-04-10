package eu.stuifzand.micropub;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.io.PrintStream;
import java.util.Formatter;

import eu.stuifzand.micropub.client.Syndication;
import eu.stuifzand.micropub.databinding.ListItemBinding;

public class LinearAdapter extends ObservableList.OnListChangedCallback<ObservableArrayList<Syndication>> {
    private final LayoutInflater inflater;
    private final Handler uiHandler;
    private LinearLayout view;

    public LinearAdapter(LinearLayout l) {
        view = l;
        inflater = (LayoutInflater) view.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        uiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onChanged(ObservableArrayList<Syndication> syndications) {
        //updateList(syndications);
        Formatter formatter = new Formatter();
        formatter.format("onChanged");
        Log.i("micropub", formatter.toString());
    }

    @Override
    public void onItemRangeChanged(ObservableArrayList<Syndication> syndications, int f, int n) {
        int l = view.getChildCount();

        uiHandler.post(() -> {
            for (int i = 0; i < n; i++) {
                Log.i("micropub", "onItemRangeChanged f=" + f + " n=" + n + " l=" + l);
                ListItemBinding binding = null;
                if (f + i < l) {
                    Log.i("micropub", "onItemRangeChanged convert: f+i=" + f + i);
                    View convertView = view.getChildAt(f + i);
                    binding = DataBindingUtil.bind(convertView);
                } else {
                    Log.i("micropub", "onItemRangeChanged new: f+i=" + f + i);
                    binding = DataBindingUtil.inflate(inflater, R.layout.list_item, view, false);
                    view.addView(binding.getRoot(), f + i);
                }
                binding.setInfo(syndications.get(f + i));
            }
        });
    }

    @Override
    public void onItemRangeInserted(ObservableArrayList<Syndication> syndications, int f, int n) {

        int l = view.getChildCount();

        uiHandler.post(() -> {
            Log.i("micropub", "onItemRangeInserted: f=" + f + " n=" + n);
            for (int i = 0; i < n; i++) {
                Log.i("micropub", "onItemRangeInserted new: f+i=" + f + i);
                {
                    ListItemBinding binding = null;
                    binding = DataBindingUtil.inflate(inflater, R.layout.list_item, view, false);
                    binding.setInfo(syndications.get(f + i));
                    view.addView(binding.getRoot(), f + i >= l ? -1 : f + i);
                }
            }
        });
    }

    @Override
    public void onItemRangeMoved(ObservableArrayList<Syndication> syndications, int f, int f2, int n) {
        Formatter formatter = new Formatter();
        formatter.format("onItemRangeMoved: %d, %d, %d", f, f2, n);
        Log.i("micropub", formatter.toString());
    }

    @Override
    public void onItemRangeRemoved(ObservableArrayList<Syndication> syndications, int f, int n) {
        uiHandler.post(() -> {
            view.removeViews(f, n);
        });
    }

    protected void updateList(ObservableArrayList<Syndication> list) {
        for (Syndication s : list) {
            ListItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item, view, false);
            binding.setInfo(s);
            view.addView(binding.getRoot());
        }
    }

    @BindingAdapter("app:multipleItems")
    public static void bindList(LinearLayout view, ObservableArrayList<Syndication> list) {
        assert list != null;
        list.addOnListChangedCallback(new LinearAdapter(view));
    }
}
