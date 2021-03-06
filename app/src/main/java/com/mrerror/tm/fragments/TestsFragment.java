package com.mrerror.tm.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mrerror.tm.MainActivity;
import com.mrerror.tm.R;
import com.mrerror.tm.adapter.TestsRecyclerViewAdapter;
import com.mrerror.tm.connection.NetworkConnection;
import com.mrerror.tm.models.Test;
import com.mrerror.tm.models.TestChoices;
import com.mrerror.tm.models.TestComplete;
import com.mrerror.tm.models.TestDialog;
import com.mrerror.tm.models.TestMistake;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mrerror.tm.connection.NetworkConnection.url;

public class TestsFragment extends Fragment implements NetworkConnection.OnCompleteFetchingData {
    // TODO: Customize parameter argument names
    private static final String ARG_TYPE = "type";
    // TODO: Customize parameters
    private String mTestUrl = null;
    private ArrayList<Test> mTestsList = null;
    private ArrayList<String> mTestsTitlesList = null;
    private TestsRecyclerViewAdapter adapter;
    ProgressBar mProgressBar;
    String noInterNet = "No_InterNet";
    String no_list = "List_is_empty";
    TextView blankText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TestsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TestsFragment newInstance(String wordsUrl) {
        TestsFragment fragment = new TestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, wordsUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTestUrl = getArguments().getString(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_testpart_list, container, false);
        mProgressBar = (ProgressBar) ((MainActivity) getActivity()).findViewById(R.id.progressbar);
        blankText = (TextView) ((MainActivity) getActivity()).findViewById(R.id.no_list_net);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mTestsList = new ArrayList<>();
            mTestsTitlesList = new ArrayList<>();

            adapter = new TestsRecyclerViewAdapter(mTestsTitlesList, mTestsList);
            getData();
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    private void getData() {
        if (isOnline()) {
            mProgressBar.setVisibility(View.VISIBLE);
            blankText.setVisibility(View.GONE);

            url = mTestUrl;
            new NetworkConnection(this).getDataAsJsonObject(getContext());
        } else {
            mProgressBar.setVisibility(View.GONE);
            blankText.setText(noInterNet);
            blankText.setVisibility(View.VISIBLE);

        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onCompleted(String result) throws JSONException {
        JSONObject testsObj = new JSONObject(result);
        if (mTestsList.size() > 0)
            mTestsList = new ArrayList<>();
        if (mTestsTitlesList.size() > 0)
            mTestsTitlesList = new ArrayList<>();
        JSONArray testsJsonArray = testsObj.getJSONArray("tests");
        for (int i = 0; i < testsJsonArray.length(); i++) {
            JSONObject testObj = testsJsonArray.getJSONObject(i);
            String testTitle = testObj.getString("title");
            mTestsTitlesList.add(testTitle);
            JSONArray choicesArrObj = testObj.getJSONArray("choices");
            ArrayList<TestChoices> testChoicesArrayList = null;
            for (int j = 0; j < choicesArrObj.length(); j++) {
                if (testChoicesArrayList == null)
                    testChoicesArrayList = new ArrayList<>();
                JSONObject choiceObj = choicesArrObj.getJSONObject(j);
                TestChoices choice = new TestChoices();
                choice.setQuestion(choiceObj.getString("question"));
                choice.setChoice1(choiceObj.getString("choice_one"));
                choice.setChoice2(choiceObj.getString("choice_two"));
                choice.setChoice3(choiceObj.getString("choice_three"));
                choice.setAnswer(choiceObj.getString("answer"));
                testChoicesArrayList.add(choice);
            }
            JSONArray completeArrObj = testObj.getJSONArray("complete");
            ArrayList<TestComplete> testCompleteArrayList = null;
            for (int j = 0; j < completeArrObj.length(); j++) {
                if (testCompleteArrayList == null)
                    testCompleteArrayList = new ArrayList<>();
                JSONObject completeObj = completeArrObj.getJSONObject(j);
                TestComplete complete = new TestComplete(completeObj.getString("description"), completeObj.getString("answer"));
                testCompleteArrayList.add(complete);
            }
            JSONArray dialogArrObj = testObj.getJSONArray("dialog");
            ArrayList<TestDialog> testDialogArrayList = null;
            for (int j = 0; j < dialogArrObj.length(); j++) {
                if (testDialogArrayList == null)
                    testDialogArrayList = new ArrayList<>();
                JSONObject dialogObj = dialogArrObj.getJSONObject(j);
                TestDialog dialog = new TestDialog(dialogObj.getString("description"), dialogObj.getString("first_speaker")
                        , dialogObj.getString("second_speaker"), dialogObj.getString("location"));
                testDialogArrayList.add(dialog);
            }
            JSONArray mistakeArrObj = testObj.getJSONArray("mistake");
            ArrayList<TestMistake> testMistakeArrayList = null;
            for (int j = 0; j < mistakeArrObj.length(); j++) {
                if (testMistakeArrayList == null)
                    testMistakeArrayList = new ArrayList<>();
                JSONObject mistakeObj = mistakeArrObj.getJSONObject(j);
                TestMistake mistake = new TestMistake(mistakeObj.getString("description"), mistakeObj.getString("replace"),
                        mistakeObj.getString("answer"));
                testMistakeArrayList.add(mistake);
            }
            Test test = new Test(testTitle, testChoicesArrayList, testCompleteArrayList, testDialogArrayList, testMistakeArrayList);
            mTestsList.add(test);
        }
        adapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.GONE);
        blankText.setVisibility(View.GONE);


    }

    @Override
    public void onError(String error) {
        mProgressBar.setVisibility(View.GONE);
        blankText.setText("an error happened ");
        blankText.setVisibility(View.VISIBLE);
    }


}
