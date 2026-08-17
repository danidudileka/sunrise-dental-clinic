/**
 * Billing management functionality
 */

let currentBillNumber = null;
let currentBillData = null;

document.addEventListener('DOMContentLoaded', function() {
    // Check if appointment number is in URL
    const urlParams = new URLSearchParams(window.location.search);
    const appointmentNumber = urlParams.get('appointment');

    if (appointmentNumber) {
        document.getElementById('bill-appointment-number').value = appointmentNumber;
        calculateBill();
    }
});

/**
 * Calculate bill preview
 */
async function calculateBill() {
    const appointmentNumber = document.getElementById('bill-appointment-number').value;

    if (!appointmentNumber) {
        showAlert('billing-error', 'Please enter an appointment number', 'error');
        return;
    }

    try {
        const response = await apiPost('/billing/calculate', {
            appointmentNumber: appointmentNumber
        });

        if (response.status === 'SUCCESS') {
            currentBillData = response.data;
            displayBillPreview(response.data);
            document.getElementById('payment-section').style.display = 'none';
        }

    } catch (error) {
        showAlert('billing-error', error.message, 'error');
        document.getElementById('bill-preview').style.display = 'none';
    }
}

/**
 * Generate bill
 */
async function generateBill() {
    const appointmentNumber = document.getElementById('bill-appointment-number').value;

    if (!appointmentNumber) {
        showAlert('billing-error', 'Please enter an appointment number', 'error');
        return;
    }

    try {
        const response = await apiPost('/billing/generate', {
            appointmentNumber: appointmentNumber
        });

        if (response.status === 'SUCCESS') {
            currentBillData = response.data;
            currentBillNumber = response.data.billNumber;
            displayBillPreview(response.data);
            document.getElementById('payment-section').style.display = 'block';

            showAlert('billing-success',
                `Bill generated successfully! Bill Number: ${response.data.billNumber}`,
                'success');
        }

    } catch (error) {
        showAlert('billing-error', error.message, 'error');
    }
}

/**
 * Display bill preview
 */
function displayBillPreview(bill) {
    const detailsBody = document.getElementById('bill-details-body');

    const rows = [
        ['Bill Number', bill.billNumber || 'Pending'],
        ['Appointment Number', bill.appointmentNumber],
        ['Patient Name', bill.patientName],
        ['Dentist', bill.dentistName],
        ['Treatment', bill.treatmentName],
        ['Treatment Cost', formatCurrency(bill.treatmentCost)],
        ['Consultation Fee', formatCurrency(bill.consultationFee)],
        ['Additional Charges', formatCurrency(bill.additionalCharges || 0)],
        ['Discount', formatCurrency(bill.discount || 0)]
    ];

    if (bill.paymentStatus) {
        rows.push(['Payment Status', bill.paymentStatus]);
    }

    if (bill.paymentMethod) {
        rows.push(['Payment Method', bill.paymentMethod]);
    }

    detailsBody.innerHTML = rows.map(([label, value]) => `
        <tr>
            <td><strong>${label}</strong></td>
            <td>${value || 'N/A'}</td>
        </tr>
    `).join('');

    document.getElementById('bill-total-amount').textContent = formatCurrency(bill.totalAmount);
    document.getElementById('bill-preview').style.display = 'block';
}

/**
 * Process payment
 */
async function processPayment() {
    if (!currentBillNumber) {
        showAlert('billing-error', 'No bill generated', 'error');
        return;
    }

    const paymentMethod = document.getElementById('payment-method').value;

    try {
        const response = await apiPost('/billing/payment', {
            billNumber: currentBillNumber,
            paymentMethod: paymentMethod
        });

        if (response.status === 'SUCCESS') {
            currentBillData = response.data;
            displayBillPreview(response.data);
            document.getElementById('payment-section').style.display = 'none';

            showAlert('billing-success', 'Payment processed successfully!', 'success');
        }

    } catch (error) {
        showAlert('billing-error', error.message, 'error');
    }
}

/**
 * Print bill
 */
function printBill() {
    if (!currentBillData) {
        return;
    }

    const printWindow = window.open('', '_blank');

    printWindow.document.write(`
        <html>
            <head>
                <title>Bill - Sunrise Dental Clinic</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        padding: 40px;
                        max-width: 600px;
                        margin: 0 auto;
                    }
                    .bill-header {
                        text-align: center;
                        margin-bottom: 30px;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #667eea;
                    }
                    .bill-header h1 {
                        color: #667eea;
                        margin-bottom: 10px;
                    }
                    .bill-header p {
                        color: #666;
                        margin: 5px 0;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 20px;
                    }
                    td {
                        padding: 10px;
                        border-bottom: 1px solid #ddd;
                    }
                    td:first-child {
                        font-weight: bold;
                        width: 40%;
                    }
                    .total {
                        text-align: right;
                        font-size: 20px;
                        font-weight: bold;
                        color: #667eea;
                        margin-top: 20px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 50px;
                        color: #999;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="bill-header">
                    <h1>Sunrise Dental Clinic</h1>
                    <p>123 Main Street, Colombo 07</p>
                    <p>Tel: +94-11-2345678</p>
                </div>
                
                <h2>Bill / Receipt</h2>
                
                <table>
                    <tr><td>Bill Number:</td><td>${currentBillData.billNumber || 'N/A'}</td></tr>
                    <tr><td>Appointment Number:</td><td>${currentBillData.appointmentNumber}</td></tr>
                    <tr><td>Patient Name:</td><td>${currentBillData.patientName}</td></tr>
                    <tr><td>Dentist:</td><td>${currentBillData.dentistName}</td></tr>
                    <tr><td>Treatment:</td><td>${currentBillData.treatmentName}</td></tr>
                    <tr><td>Treatment Cost:</td><td>${formatCurrency(currentBillData.treatmentCost)}</td></tr>
                    <tr><td>Consultation Fee:</td><td>${formatCurrency(currentBillData.consultationFee)}</td></tr>
                    <tr><td>Additional Charges:</td><td>${formatCurrency(currentBillData.additionalCharges || 0)}</td></tr>
                    <tr><td>Discount:</td><td>${formatCurrency(currentBillData.discount || 0)}</td></tr>
                    <tr><td>Payment Status:</td><td>${currentBillData.paymentStatus || 'PENDING'}</td></tr>
                    <tr><td>Payment Method:</td><td>${currentBillData.paymentMethod || 'N/A'}</td></tr>
                    <tr><td>Date:</td><td>${new Date().toLocaleString()}</td></tr>
                </table>
                
                <div class="total">
                    Total Amount: ${formatCurrency(currentBillData.totalAmount)}
                </div>
                
                <div class="footer">
                    <p>Thank you for choosing Sunrise Dental Clinic!</p>
                    <p>&copy; 2024 Sunrise Dental Clinic. All rights reserved.</p>
                </div>
                
                <script>
                    window.onload = function() {
                        window.print();
                    }
                <\/script>
            </body>
        </html>
    `);

    printWindow.document.close();
}