package eu.stuifzand.micropub;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import eu.stuifzand.micropub.client.Syndication;
import eu.stuifzand.micropub.databinding.ListItemBinding;

public class ListAdapter extends BaseAdapter {
    private ObservableArrayList<Syndication> list;
    private LayoutInflater inflater;

    public ListAdapter(ObservableArrayList<Syndication> l) {
        list = l;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        ListItemBinding binding = null;

        if (convertView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.list_item, parent, false);
        } else {
            binding = DataBindingUtil.bind(convertView);
        }

        binding.setInfo(list.get(position));
        return binding.getRoot();
    }

    @BindingAdapter("list")
    public static void bindList(ListView view, ObservableArrayList<Syndication> list) {
        assert list != null;
        ListAdapter adapter = new ListAdapter(list);
        view.setAdapter(adapter);
    }
}