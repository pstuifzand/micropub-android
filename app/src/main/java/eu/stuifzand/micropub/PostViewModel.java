package eu.stuifzand.micropub;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;


public class PostViewModel extends ViewModel {
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<String> category = new ObservableField<>();

    public PostViewModel() {
        this.content.set("");
        this.category.set("");
    }

    public void clear() {
        this.content.set("");
        this.category.set("");
    }
}
