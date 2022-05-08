package com.xiangning.simplelauncher.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xiangning.sectionadapter.SectionAdapter
import com.xiangning.sectionadapter.binder.SimpleItemBinder
import com.xiangning.simplelauncher.entity.ContactItem
import com.xiangning.simplelauncher.R
import kotlinx.android.synthetic.main.activity_contacts.*


class Contacts : BaseActivity() {

    private val PICK_CONTACT: Int = 111
    private var section: SectionAdapter.Section? = null
    private val items: MutableList<ContactItem> = mutableListOf()


    private val KEY_CONTACTS = "key_contacts"

    private val gson = Gson()
    private var sp: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        sp = getSharedPreferences("default", Context.MODE_PRIVATE)

        sp!!.getString(KEY_CONTACTS, null)?.let {
            val list: List<ContactItem> = gson.fromJson(it,  object : TypeToken<List<ContactItem>?>() {}.type)
            if (!list.isNullOrEmpty()) {
                items.addAll(list)
            }
        }

        val isEdit = intent.getBooleanExtra("isEdit", false)
        if (isEdit) {
            tv_title.text = "编辑联系人"
            add_contact.visibility = View.VISIBLE
            add_contact.setOnClickListener { selectContact() }
        }

        val adapter = SectionAdapter()
        section = adapter.register(
            ContactItem::class.java,
            SimpleItemBinder(R.layout.layout_contacts_list) { holder, item ->
                if (item.avatar != null)
                    holder.get<ImageView>(R.id.avatar).setImageDrawable(item.avatar)

                holder.get<TextView>(R.id.name).text = item.name
                holder.itemView.setOnClickListener {
                    if (!isEdit && !item.phone.isNullOrBlank()) {
                        callPhone(item.phone)
                    }
                }

                if (isEdit) {
                    holder.itemView.setOnLongClickListener {
                        remove(item)
                        return@setOnLongClickListener true
                    }
                }
            }
        )
        section?.setItems(items)

        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun remove(item: ContactItem) {
        items.remove(item)
        section?.setItems(items)
        updateContactToSp()
    }

    private fun add(item: ContactItem) {
        items.add(item)
        section?.setItems(items)
        updateContactToSp()
    }

    private fun updateContactToSp() {
        sp?.edit()?.putString(KEY_CONTACTS, gson.toJson(items))?.apply()
    }

    private fun selectContact() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE;
        startActivityForResult(intent, PICK_CONTACT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            readContacts(data!!)
        }
    }

    private fun readContacts(data: Intent) {
        val uri = data.data ?: return
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursor = contentResolver.query(uri, projection, null, null, null) ?: return
        if (cursor.moveToFirst()) {
            val nameIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            val name = cursor.getString(nameIndex) //联系人姓名
            val number = cursor.getString(numberIndex) //联系人号码
            add(ContactItem(name, null, number))
            cursor.close()
        }
    }

    private fun callPhone(phoneNum: String) {
        val intent = Intent(Intent.ACTION_CALL)
        val data: Uri = Uri.parse("tel:$phoneNum")
        intent.data = data
        startActivity(intent)
    }
}
