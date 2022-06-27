package com.onepay.onepaygo.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.onepay.onepaygo.R
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.TerminalDataSource
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.databinding.ActivityMoreDetailsBinding


class MoreDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMoreDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        Logger.info("", "onCreate", "MoreDetailsActivity")

    }

    fun initView() {
        val moreDetails = TransactionHistoryDataSource.getTransactionItem()
        val stringBuffer = StringBuffer(moreDetails!!.Transaction.ExpirationDate)
        binding.tvAccountType.text = moreDetails.Transaction.AccountType
        if (moreDetails.CardNumber.length > 5)
            binding.accountNumber.text = moreDetails.CardNumber.substring(
                moreDetails.CardNumber.length - 5,
                moreDetails.CardNumber.length
            )
        if (stringBuffer.length > 2)
            binding.tvExpDate.text = stringBuffer.insert(2, "/").toString()
        binding.tvTransactionAmount.text = moreDetails.CaptureAmount
        binding.tvPaymentMethod.text = moreDetails.Transaction.Method
        binding.tvToken.text = moreDetails.Transaction.Token
        binding.tvEntryMode.text = moreDetails.Transaction.POSEntryModeDesc
        binding.tvSettleAmount.text = moreDetails.SettlementAmount
        if (!moreDetails.Transaction.SettlementDate.isNullOrEmpty())
            binding.tvSettlementDateTime.text = Utils.getTransactionDateMore(moreDetails.Transaction.SettlementDate)
        if (moreDetails.Transaction.SettledStatus == 1) {
            binding.tvSettlementStatus.text = resources.getString(R.string.settled)
        } else if (moreDetails.Transaction.SettledStatus == 2 || moreDetails.Transaction.SettledStatus == 3)
            binding.tvSettlementStatus.text = resources.getString(R.string.void_str)
        else binding.tvSettlementStatus.text = resources.getString(R.string.unsettled)
        binding.tvAuthorizedAmount.text = moreDetails.ApprovedAmount
        if (moreDetails.Transaction.MerchantTransactionDateTime != null)
            binding.tvTransactionDateTimeMore.text = Utils.getTransactionDateMore(moreDetails.Transaction.MerchantTransactionDateTime)
        binding.tvAuthorizationCode.text = moreDetails.Transaction.AuthID
        binding.tvRefTransactionId.text =
            getValidString(moreDetails.Transaction.ReferenceTransactionId)
        binding.tvRelatedTransactionId.text =
            getValidString(moreDetails.Transaction.RelatedTransactionID!!)
        binding.tvTransactionNotes.text = moreDetails.Transaction.TransactionNotes
        binding.tvTransactionType.text = moreDetails.TransactionType
        binding.tvTransactionId.text = moreDetails.Transaction.Id.toString()
        binding.tvTerminalId.text = moreDetails.Transaction.TerminalId
        binding.tvTerminalNameMore.text =
            TerminalDataSource.getTerminalByID(moreDetails.Transaction.MerchantTerminalID)
        binding.tvMarketType.text = moreDetails.Transaction.IndustryCode
        binding.tvProduct.text = moreDetails.Transaction.ProductDescription
        binding.tvAddressVerificationStatus.text = moreDetails.Transaction.AVSResultCode
        binding.tvCardCodeStatus.text = moreDetails.Transaction.card_class
        binding.tvCcvResultCode.text = moreDetails.Transaction.CVVResultCode
        binding.tvTest.text = moreDetails.Transaction.Test.toString()


        binding.tvCustomerIp.text = moreDetails.Transaction.ClientIP
        binding.tvSolution.text = moreDetails.Transaction.SourceApplication
        binding.tvSrcUser.text = moreDetails.Transaction.SourceUser
        binding.tvSrcIp.text = moreDetails.Transaction.SourceIP

        binding.tvInvoiceNoMore.text = moreDetails.Transaction.InvoiceNumber
        binding.tvNonce.text = moreDetails.Transaction.Nonce


        binding.tvName.text = String.format("%s %s", moreDetails.Transaction.FirstName, moreDetails.Transaction.LastName)
        binding.tvCompany.text = moreDetails.Transaction.Company
        binding.tvAddress.text = String.format("%s %s", moreDetails.Transaction.Street1, moreDetails.Transaction.Street2)
        binding.tvCity.text = moreDetails.Transaction.City
        binding.tvState.text = moreDetails.Transaction.State
        binding.tvCountry.text = moreDetails.Transaction.Country
        binding.tvZip.text = moreDetails.Transaction.Zip
        binding.tvPno.text = moreDetails.Transaction.PhoneNumber
        binding.tvEmail.text = moreDetails.Transaction.Email
        binding.tvCustomerId.text = moreDetails.Transaction.CustomerId
        binding.tvEmailReceipt.text = moreDetails.Transaction.EmailReceipt


        if (moreDetails.Transaction.L2Id != 0) {
            binding.llLavel2.visibility = View.VISIBLE
            binding.tvTaxAmount.text = moreDetails.Transaction.TaxAmount.toString()
            binding.tvTaxIndicator.text = moreDetails.Transaction.TaxIndicator.toString()
            binding.tvVatAltAmount.text = moreDetails.Transaction.VatALtTaxRate.toString()
            binding.tvDiscount.text = moreDetails.Transaction.DiscountAmount.toString()
            binding.tvDestPostal.text = moreDetails.Transaction.DestPostalCode
            binding.tvShipFrom.text = moreDetails.Transaction.ShipFromPostalCode
            binding.tvDestCountry.text = moreDetails.Transaction.DestCountryCode.toString()
            binding.tvProduct.text = moreDetails.Transaction.ProductDescription.toString()
        } else binding.llLavel2.visibility = View.GONE

        if (moreDetails.Transaction.L3Id != 0) {
            binding.llLavel3.visibility = View.VISIBLE
            binding.tvItemSeqNum.text = moreDetails.Transaction.ItemSeqNum.toString()
            binding.tvItemCode.text = moreDetails.Transaction.ItemCode.toString()
            binding.tvItemDescription.text = moreDetails.Transaction.ItemDescription
            binding.tvLvl3ItemQuantity.text = moreDetails.Transaction.Lvl3ItemQuantity.toString()
            binding.tvLvl3UnitCost.text = moreDetails.Transaction.Lvl3UnitCost.toString()
            binding.tvLvl3UnitofMeasure.text = moreDetails.Transaction.Lvl3UnitofMeasure.toString()
            binding.tvLvl3LineItemTotal.text =
                moreDetails.Transaction.Lvl3LineItemTotal.toString()
            binding.tvLvl3DiscountAmount.text =
                moreDetails.Transaction.Lvl3DiscountAmount.toString()
            binding.tvLvl3VatAltTaxAmount.text =
                moreDetails.Transaction.Lvl3VatAltTaxAmount.toString()
            binding.tvLvl3VatAltTaxRate.text =
                moreDetails.Transaction.Lvl3VatAltTaxRate.toString()
            binding.tvLvl3ProductCode.text = moreDetails.Transaction.Lvl3ProductCode.toString()
            binding.tvLvl3ExtendedItemAmount.text =
                moreDetails.Transaction.Lvl3ExtendedItemAmount.toString()
            binding.tvLvl3TaxIndicator.text = moreDetails.Transaction.Lvl3TaxIndicator.toString()
            binding.tvLvl3TaxRate.text = moreDetails.Transaction.Lvl3TaxRate.toString()
            binding.tvLvl3TaxAmount.text = moreDetails.Transaction.Lvl3TaxAmount.toString()
            binding.tvLvl3ProductDescriptor.text =
                moreDetails.Transaction.Lvl3ProductDescriptor.toString()
            binding.tvLvl3TaxTypeIdentifier.text =
                moreDetails.Transaction.Lvl3TaxTypeIdentifier.toString()
        } else binding.llLavel3.visibility = View.GONE

        binding.tvResultCode.text = moreDetails.Transaction.ResultCode
        binding.tvResultSubCode.text = moreDetails.Transaction.ResultSubCode
        binding.tvResultText.text = moreDetails.Transaction.ResultText
        binding.tvBatchId.text = getValidString(moreDetails.Transaction.BatchId)
        binding.tvAuthNtwk.text = moreDetails.Transaction.AuthNtwkName
        binding.tvProcessorId.text = moreDetails.Transaction.ProcessorTranID
        binding.tvProcessorAci.text = moreDetails.Transaction.ProcessorACI
        binding.tvProcessorCardLRC.text = moreDetails.Transaction.ProcessorCardLevelResultCode

        binding.tvCardInfo.text = moreDetails.Transaction.CardCaptCap
        binding.tvCardClass.text = moreDetails.Transaction.card_class
        binding.tvProductId.text = moreDetails.Transaction.product_id
        binding.tvPrepaidIndicator.text = moreDetails.Transaction.prepaid_indicator
        binding.tvDetailCardIndicator.text = moreDetails.Transaction.detailcard_indicator
        binding.tvDebitNetworkIndicator.text = moreDetails.Transaction.debitnetwork_indicator

        val customFields = TransactionHistoryDataSource.getCustomFieldList()
        for (item in customFields) {
            when (item.UDFNumber) {
                1 -> {
                    if(moreDetails.Transaction.UserDefined1.isEmpty())return
                    binding.llCustom1.visibility = View.VISIBLE
                    binding.tvCustom1.text = moreDetails.Transaction.UserDefined1
                    binding.tvLabel1.text = String.format("%s:",item.Label)
                }
                2 -> {
                    if(moreDetails.Transaction.UserDefined2.isEmpty())return
                    binding.llCustom2.visibility = View.VISIBLE
                    binding.tvCustom2.text = moreDetails.Transaction.UserDefined2
                    binding.tvLabel2.text = String.format("%s:",item.Label)
                }
                3 -> {                    if(moreDetails.Transaction.UserDefined3.isEmpty())return
                    binding.llCustom3.visibility = View.VISIBLE
                    binding.tvCustom3.text = moreDetails.Transaction.UserDefined3
                    binding.tvLabel3.text = String.format("%s:",item.Label)
                }
                4 -> {
                    if(moreDetails.Transaction.UserDefined4.isEmpty())return
                    binding.llCustom4.visibility = View.VISIBLE
                    binding.tvCustom4.text = moreDetails.Transaction.UserDefined4
                    binding.tvLabel4.text = String.format("%s:",item.Label)
                }
                5 -> {
                    if(moreDetails.Transaction.UserDefined5.isEmpty())return
                    binding.llCustom5.visibility = View.VISIBLE
                    binding.tvCustom5.text = moreDetails.Transaction.UserDefined5
                    binding.tvLabel5.text = String.format("%s:",item.Label)
                }
                6 -> {
                    if(moreDetails.Transaction.UserDefined6.isEmpty())return
                    binding.llCustom6.visibility = View.VISIBLE
                    binding.tvCustom6.text = moreDetails.Transaction.UserDefined6
                    binding.tvLabel6.text = String.format("%s:",item.Label)
                }
                7 -> {
                    if(moreDetails.Transaction.UserDefined7.isEmpty())return
                    binding.llCustom7.visibility = View.VISIBLE
                    binding.tvCustom7.text = moreDetails.Transaction.UserDefined7
                    binding.tvLabel7.text = String.format("%s:",item.Label)
                }
                8 -> {
                    if(moreDetails.Transaction.UserDefined8.isEmpty())return
                    binding.llCustom8.visibility = View.VISIBLE
                    binding.tvCustom8.text = moreDetails.Transaction.UserDefined8
                    binding.tvLabel8.text = String.format("%s:",item.Label)
                }
                9 -> {
                    if(moreDetails.Transaction.UserDefined9.isEmpty())return
                    binding.llCustom9.visibility = View.VISIBLE
                    binding.tvCustom9.text = moreDetails.Transaction.UserDefined9
                    binding.tvLabel9.text = String.format("%s:",item.Label)
                }
                10 -> {
                    if(moreDetails.Transaction.UserDefined10.isEmpty())return
                    binding.llCustom10.visibility = View.VISIBLE
                    binding.tvCustom10.text = moreDetails.Transaction.UserDefined10
                    binding.tvLabel10.text = String.format("%s:",item.Label)
                }
                11 -> {
                    if(moreDetails.Transaction.UserDefined11.isEmpty())return
                    binding.llCustom11.visibility = View.VISIBLE
                    binding.tvCustom11.text = moreDetails.Transaction.UserDefined11
                    binding.tvLabel11.text = String.format("%s:",item.Label)
                }
                12 -> {
                    if(moreDetails.Transaction.UserDefined12.isEmpty())return
                    binding.llCustom12.visibility = View.VISIBLE
                    binding.tvCustom12.text = moreDetails.Transaction.UserDefined12
                    binding.tvLabel12.text = String.format("%s:",item.Label)
                }
                13 -> {
                    if(moreDetails.Transaction.UserDefined13.isEmpty())return
                    binding.llCustom13.visibility = View.VISIBLE
                    binding.tvCustom13.text = moreDetails.Transaction.UserDefined13
                    binding.tvLabel13.text = String.format("%s:",item.Label)
                }
                14 -> {
                    if(moreDetails.Transaction.UserDefined14.isEmpty())return
                    binding.llCustom14.visibility = View.VISIBLE
                    binding.tvCustom14.text = moreDetails.Transaction.UserDefined14
                    binding.tvLabel14.text = String.format("%s:",item.Label)
                }
                15 -> {
                    if(moreDetails.Transaction.UserDefined15.isEmpty())return
                    binding.llCustom15.visibility = View.VISIBLE
                    binding.tvCustom15.text = moreDetails.Transaction.UserDefined15
                    binding.tvLabel15.text = String.format("%s:",item.Label)
                }
                16 -> {
                    if(moreDetails.Transaction.UserDefined16.isEmpty())return
                    binding.llCustom16.visibility = View.VISIBLE
                    binding.tvCustom16.text = moreDetails.Transaction.UserDefined16
                    binding.tvLabel16.text = String.format("%s:",item.Label)
                }
                17 -> {
                    if(moreDetails.Transaction.UserDefined17.isEmpty())return
                    binding.llCustom17.visibility = View.VISIBLE
                    binding.tvCustom17.text = moreDetails.Transaction.UserDefined17
                    binding.tvLabel17.text = String.format("%s:",item.Label)
                }
                18 -> {
                    if(moreDetails.Transaction.UserDefined18.isEmpty())return
                    binding.llCustom18.visibility = View.VISIBLE
                    binding.tvCustom18.text = moreDetails.Transaction.UserDefined18
                    binding.tvLabel18.text = String.format("%s:",item.Label)
                }
                19 -> {
                    if(moreDetails.Transaction.UserDefined19.isEmpty())return
                    binding.llCustom19.visibility = View.VISIBLE
                    binding.tvCustom19.text = moreDetails.Transaction.UserDefined19
                    binding.tvLabel19.text = String.format("%s:",item.Label)
                }
                20 -> {
                    if(moreDetails.Transaction.UserDefined20.isEmpty())return
                    binding.llCustom20.visibility = View.VISIBLE
                    binding.tvCustom20.text = moreDetails.Transaction.UserDefined20
                    binding.tvLabel20.text = String.format("%s:",item.Label)
                }


            }
        }
//        if(customFields.size>0){
//            val ll = LinearLayout(this)
//            ll.orientation = LinearLayout.HORIZONTAL
//            val layoutParams: LinearLayout.LayoutParams =
//                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//            layoutParams.setMargins(25, 20, 25, 10)
//            val tv = TextView(this)
//            tv.setText(customFields.get(0).Label)
//            tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//            val tvValue = TextView(this)
//            tvValue.setText("customFields.get(0).Label")
//            tvValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//            tvValue.setTextAppearance(R.style.)
//            ll.addView(tv)
//            ll.addView(tvValue)
//            binding.llCustomField.addView(ll)
//        }
    }

    fun getValidString(data: Int): String {
        if (data === 0) return "-"
        else return data.toString()
    }
}