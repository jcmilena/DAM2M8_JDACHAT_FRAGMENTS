package com.example.juancarlosmilena.jdachat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnUserFragmentListener mListener;

    RecyclerView usersRecycler;
    List<User> userList = new ArrayList<>();
    usersAdapter miAdapter = new usersAdapter(userList);


    public UsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        usersRecycler = view.findViewById(R.id.users_recycler);
        usersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecycler.setAdapter(miAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL);
        usersRecycler.addItemDecoration(dividerItemDecoration);

        mListener.getFirebaseUsers();


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserFragmentListener) {
            mListener = (OnUserFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnUserFragmentListener {
        // TODO: Update argument type and name
        void getFirebaseUsers();
        void sendMessage(String chat);

    }

    public class UserViewHolder extends RecyclerView.ViewHolder{

        TextView emailTextView;


        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            emailTextView = itemView.findViewById(R.id.emailTextView);

            emailTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Llamamos al Controller para pedirle que queremos enviar
                    //un mensaje al chat del usuario seleccionado
                    mListener.sendMessage(userList.get(getAdapterPosition()).getChat());
                }
            });
        }
    }

    public class usersAdapter extends RecyclerView.Adapter<UserViewHolder>{

        List<User> userslistadapter = new ArrayList<>();

        public usersAdapter(List<User> userslistadapter) {
            this.userslistadapter = userslistadapter;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemview = getLayoutInflater()
                    .inflate(R.layout.user_viewholder, viewGroup, false);
            return new UserViewHolder(itemview);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i) {

            userViewHolder.emailTextView
                    .setText(userslistadapter.get(i).getEmail());

        }

        @Override
        public int getItemCount() {
            return userslistadapter.size();
        }
    }

    public void addUserToList(User user){

        userList.add(user);
        miAdapter.notifyDataSetChanged();
    }
}
