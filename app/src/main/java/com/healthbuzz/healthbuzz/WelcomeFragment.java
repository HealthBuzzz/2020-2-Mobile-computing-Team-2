package com.healthbuzz.healthbuzz;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        UserInfo.INSTANCE.getUserName().observe(this, aString -> {
            MenuItem item = menu.findItem(R.id.login);
            if(aString.equals("")) {
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