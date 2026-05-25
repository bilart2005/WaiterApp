package com.waiter.app.ui.info;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.waiter.app.R;

public class InfoDataModelFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_info_datamodel, c, false);
    }
}
