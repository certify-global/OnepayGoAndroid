package com.onepay.onepaygo.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.TerminalDataSource
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.databinding.ActivityMoreDetailsBinding

class MoreDetailsActivity : AppCompatActivity() {
    private val TAG = HistoryDetailsActivity::class.java.name
    private lateinit var binding: ActivityMoreDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

    }
    fun initView(){
        val moreDetails = TransactionHistoryDataSource.getTransactionItem()
        val stringBuffer = StringBuffer(moreDetails?.Transaction?.ExpirationDate)
        binding.tvAccountType.text = moreDetails?.Transaction?.AccountType
        if(moreDetails?.CardNumber !=null && moreDetails?.CardNumber?.length>5)
        binding.accountNumber.text = moreDetails?.CardNumber?.substring(moreDetails?.CardNumber?.length-5,moreDetails?.CardNumber?.length)
        if(stringBuffer.length>2)
        binding.tvExpDate.text = stringBuffer.insert(2,"/").toString()
        binding.tvTransactionAmount.text = moreDetails?.CaptureAmount
        binding.tvPaymentMethod.text = moreDetails?.Transaction?.Method
        binding.tvToken.text = moreDetails?.Transaction?.Token
        binding.tvEntryMode.text = moreDetails?.Transaction?.POSEntryModeDesc
        binding.tvSettleAmount.text = moreDetails?.SettlementAmount
        if(moreDetails?.Transaction?.SettlementDate != null)
        binding.tvSettlementDateTime.text = Utils.getTransactionDateMore(moreDetails?.Transaction?.SettlementDate!!)
        if (moreDetails?.Transaction?.SettledStatus === 1) {
            binding.tvSettlementStatus.text=("Settled")
        } else if(moreDetails?.Transaction?.SettledStatus ===2 ||moreDetails?.Transaction?.SettledStatus ===3)
            binding.tvSettlementStatus.text=("Void")
        else  binding.tvSettlementStatus.text =("Unsettled")
        binding.tvAuthorizedAmount.text = moreDetails?.ApprovedAmount
        if(moreDetails?.Transaction?.MerchantTransactionDateTime != null)
        binding.tvTransactionDateTimeMore.text = Utils.getTransactionDateMore(moreDetails?.Transaction?.MerchantTransactionDateTime)
        binding.tvAuthorizationCode.text = moreDetails?.Transaction?.AuthID
        binding.tvRefTransactionId.text = getValidString(moreDetails?.Transaction?.ReferenceTransactionId!!)
        binding.tvRelatedTransactionId.text = getValidString(moreDetails?.Transaction?.RelatedTransactionID!!)
        binding.tvTransactionNotes.text = moreDetails?.Transaction?.TransactionNotes
        binding.tvTransactionType.text = moreDetails?.TransactionType
        binding.tvTransactionId.text = moreDetails?.Transaction?.Id.toString()
        binding.tvTerminalId.text = moreDetails?.Transaction?.TerminalId
        binding.tvTerminalNameMore.text = TerminalDataSource.getTerminalByID(moreDetails?.Transaction.MerchantTerminalID)
        binding.tvMarketType.text = moreDetails?.Transaction?.IndustryCode
        binding.tvProduct.text = moreDetails?.Transaction?.ProductDescription
        binding.tvAddressVerificationStatus.text = moreDetails?.Transaction?.AVSResultCode
        binding.tvCardCodeStatus.text = moreDetails?.Transaction?.card_class
        binding.tvCcvResultCode.text = moreDetails?.Transaction?.CVVResultCode
        binding.tvTest.text = moreDetails?.Transaction?.Test.toString()


        binding.tvCustomerIp.text = moreDetails?.Transaction?.ClientIP
        binding.tvSolution.text = moreDetails?.Transaction?.SourceApplication
        binding.tvSrcUser.text = moreDetails?.Transaction?.SourceUser
        binding.tvSrcIp.text = moreDetails?.Transaction?.SourceIP

        binding.tvInvoiceNoMore.text = moreDetails?.Transaction?.InvoiceNumber
        binding.tvNonce.text = moreDetails?.Transaction?.Nonce


        binding.tvName.text = String.format("%s %s",moreDetails?.Transaction?.FirstName,moreDetails?.Transaction?.LastName)
        binding.tvCompany.text = moreDetails?.Transaction.Company
        binding.tvAddress.text = String.format("%s %s",moreDetails?.Transaction?.Street1,moreDetails?.Transaction?.Street2)
        binding.tvCity.text = moreDetails?.Transaction?.City
        binding.tvState.text = moreDetails?.Transaction?.State
        binding.tvCountry.text = moreDetails?.Transaction?.Country
        binding.tvZip.text =moreDetails?.Transaction?.Zip
        binding.tvPno.text =moreDetails?.Transaction?.PhoneNumber
        binding.tvEmail.text =moreDetails?.Transaction?.Email
        binding.tvCustomerId.text = moreDetails?.Transaction?.CustomerId
        binding.tvEmailReceipt.text = moreDetails?.Transaction?.EmailReceipt


if(moreDetails.Transaction.L2Id !=0) {
    binding.llLavel2.visibility = View.VISIBLE
    binding.tvTaxAmount.text = moreDetails?.Transaction?.TaxAmount.toString()
    binding.tvTaxIndicator.text = moreDetails?.Transaction.TaxIndicator.toString()
    binding.tvVatAltAmount.text = moreDetails?.Transaction.VatALtTaxRate.toString()
    binding.tvDiscount.text = moreDetails?.Transaction?.DiscountAmount.toString()
    binding.tvDestPostal.text = moreDetails?.Transaction.DestPostalCode
    binding.tvShipFrom.text = moreDetails?.Transaction.ShipFromPostalCode
    binding.tvDestCountry.text = moreDetails?.Transaction?.DestCountryCode.toString()
    binding.tvProduct.text = moreDetails?.Transaction.ProductDescription.toString()
}else binding.llLavel2.visibility = View.GONE

        if(moreDetails.Transaction.L3Id !=0) {
            binding.llLavel3.visibility = View.VISIBLE
            binding.tvItemSeqNum.text = moreDetails?.Transaction.ItemSeqNum.toString()
            binding.tvItemCode.text = moreDetails?.Transaction?.ItemCode.toString()
            binding.tvItemDescription.text = moreDetails?.Transaction.ItemDescription
            binding.tvLvl3ItemQuantity.text = moreDetails?.Transaction.Lvl3ItemQuantity.toString()
            binding.tvLvl3UnitCost.text = moreDetails?.Transaction?.Lvl3UnitCost.toString()
            binding.tvLvl3UnitofMeasure.text = moreDetails?.Transaction.Lvl3UnitofMeasure.toString()
            binding.tvLvl3LineItemTotal.text =
                moreDetails?.Transaction?.Lvl3LineItemTotal.toString()
            binding.tvLvl3DiscountAmount.text =
                moreDetails?.Transaction.Lvl3DiscountAmount.toString()
            binding.tvLvl3VatAltTaxAmount.text =
                moreDetails?.Transaction.Lvl3VatAltTaxAmount.toString()
            binding.tvLvl3VatAltTaxRate.text =
                moreDetails?.Transaction?.Lvl3VatAltTaxRate.toString()
            binding.tvLvl3ProductCode.text = moreDetails?.Transaction.Lvl3ProductCode.toString()
            binding.tvLvl3ExtendedItemAmount.text =
                moreDetails?.Transaction?.Lvl3ExtendedItemAmount.toString()
            binding.tvLvl3TaxIndicator.text = moreDetails?.Transaction.Lvl3TaxIndicator.toString()
            binding.tvLvl3TaxRate.text = moreDetails?.Transaction.Lvl3TaxRate.toString()
            binding.tvLvl3TaxAmount.text = moreDetails?.Transaction?.Lvl3TaxAmount.toString()
            binding.tvLvl3ProductDescriptor.text =
                moreDetails?.Transaction.Lvl3ProductDescriptor.toString()
            binding.tvLvl3TaxTypeIdentifier.text =
                moreDetails?.Transaction?.Lvl3TaxTypeIdentifier.toString()
        }else binding.llLavel3.visibility = View.GONE

        binding.tvResultCode.text = moreDetails?.Transaction?.ResultCode
        binding.tvResultSubCode.text = moreDetails?.Transaction?.ResultSubCode
        binding.tvResultText.text = moreDetails?.Transaction?.ResultText
        binding.tvBatchId.text = getValidString(moreDetails?.Transaction?.BatchId)
        binding.tvAuthNtwk.text = moreDetails?.Transaction?.AuthNtwkName
        binding.tvProcessorId.text = moreDetails?.Transaction?.ProcessorTranID
        binding.tvProcessorAci.text =moreDetails?.Transaction?.ProcessorACI
        binding.tvProcessorCardLRC.text =moreDetails?.Transaction?.ProcessorCardLevelResultCode

        binding.tvCardInfo.text =moreDetails?.Transaction?.CardCaptCap
        binding.tvCardClass.text = moreDetails?.Transaction.card_class
        binding.tvProductId.text = moreDetails?.Transaction.product_id
        binding.tvPrepaidIndicator.text =moreDetails?.Transaction.prepaid_indicator
        binding.tvDetailCardIndicator.text = moreDetails?.Transaction.detailcard_indicator
        binding.tvDebitNetworkIndicator.text = moreDetails?.Transaction.debitnetwork_indicator
    }
    fun getValidString(data:Int) :String{
        if(data===0) return "-"
        else return data.toString()
    }
}