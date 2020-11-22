package com.healthbuzz.healthbuzz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.healthbuzz.healthbuzz.data.LoginDataSource;
import com.healthbuzz.healthbuzz.ui.login.LoginActivity;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WelcomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WelcomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WelcomeFragment newInstance() {
        WelcomeFragment fragment = new WelcomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UserInfo.INSTANCE.getUserName().observe(this, aString -> {
            TextView userView = (TextView) getView().findViewById(R.id.textView2);
            userView.setText(aString);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView manView = getView().findViewById(R.id.man);
        ImageView treeView = getView().findViewById(R.id.tree);
        RealtimeModel.INSTANCE.getStretching_count().observe(getViewLifecycleOwner(), stretching_count -> {
            int int_count = stretching_count.intValue();
            if (int_count >= 6) {
                int_count = 5;
            }
            switch (int_count) {
                case 0:
                case 1:
                    manView.setImageResource(R.drawable.man1);
                    break;
                case 2:
                case 3:
                    manView.setImageResource(R.drawable.man2);
                    break;
                case 4:
                case 5:
                    manView.setImageResource(R.drawable.man3);
                    break;
                default:
                    //throw new IllegalStateException();
            }
        });
        RealtimeModel.INSTANCE.getWater_count().observe(getViewLifecycleOwner(), water_count -> {
            int int_count = water_count.intValue();
            int_count /= 1000;
            if (int_count >= 3) {
                int_count = 2;
            }
            switch (int_count) {
                case 0:
                    treeView.setImageResource(R.drawable.tree1);
                    break;
                case 1:
                    treeView.setImageResource(R.drawable.tree2);
                    break;
                case 2:
                    treeView.setImageResource(R.drawable.tree3);
                    break;
                default:
                    //throw new IllegalStateException();
            }
        });
    }

    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        UserInfo.INSTANCE.getUserName().observe(this, aString -> {
            MenuItem item = menu.findItem(R.id.login);
            if (aString.equals("")) {
                item.setTitle("Login");
            } else {
                item.setTitle("Logout");
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.settings) {
            Intent intent = new Intent(requireActivity(), SettingsActivity.class);
            startActivity(intent);
//                Toast.makeText(getActivity(), "Go to Settings page", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.debug) {
//            Toast.makeText(getActivity(), "Go to Debug page", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), DataGettingActivity.class));
            return true;
        } else if (itemId == R.id.help) {
//            Toast.makeText(getActivity(), "Go to help page", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), InferenceActivity.class));
            return true;
        } else if (itemId == R.id.debug_drinking) {
//            Toast.makeText(getActivity(), "Go to help page", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), DrinkingDataGettingActivity.class));
            return true;
        } else if (itemId == R.id.debug_movement) {
//            Toast.makeText(getActivity(), "Go to help page", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), MovementDataGettingActivity.class));
            return true;
        } else if (itemId == R.id.login) {
//            Toast.makeText(getActivity(), "Go to help page", Toast.LENGTH_SHORT).show();
            // If user wants login
            if (UserInfo.INSTANCE.getUserName().getValue().equals("")) {
                startActivity(new Intent(requireActivity(), LoginActivity.class));
            } else {
                LoginDataSource.logout();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}