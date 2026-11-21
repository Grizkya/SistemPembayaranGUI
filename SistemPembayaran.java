import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/* ===================== INTERFACE ===================== */

interface DapatDicicil {
    double hitungCicilan(int tenorBulan);   // cicilan per bulan
}

interface DapatDibatalkan {
    String batalkan();
}

/* ===================== ABSTRACT CLASS ===================== */

abstract class Pembayaran {
    protected String idTransaksi;
    protected double jumlah;
    protected String status = "MENUNGGU";

    public Pembayaran(String idTransaksi, double jumlah) {
        this.idTransaksi = idTransaksi;
        this.jumlah = jumlah;
    }

    // Method abstrak: tiap metode pembayaran punya cara proses sendiri
    public abstract String prosesPembayaran();

    // Format Rupiah manual (titik ribuan, koma desimal) tanpa Locale
    public static String formatRupiah(double nilai) {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols();
        simbol.setGroupingSeparator('.');
        simbol.setDecimalSeparator(',');
        return "Rp " + new DecimalFormat("#,##0.00", simbol).format(nilai);
    }

    // Method konkret: format struk yang sama untuk semua metode
    public String cetakStruk() {
        return "=========== STRUK PEMBAYARAN ===========\n"
             + "ID Transaksi : " + idTransaksi + "\n"
             + "Metode       : " + getClass().getSimpleName() + "\n"
             + "Jumlah       : " + formatRupiah(jumlah) + "\n"
             + "Status       : " + status + "\n"
             + "========================================\n";
    }

    public String getStatus() { return status; }
    public String getIdTransaksi() { return idTransaksi; }
}

/* ===================== SUBCLASS ===================== */

// Mendukung cicilan DAN pembatalan
class PembayaranKartuKredit extends Pembayaran implements DapatDicicil, DapatDibatalkan {
    private String nomorKartu;

    public PembayaranKartuKredit(String id, double jumlah, String nomorKartu) {
        super(id, jumlah);
        this.nomorKartu = nomorKartu;
    }

    @Override
    public String prosesPembayaran() {
        status = "BERHASIL";
        String masked = "**** **** **** " + nomorKartu.substring(Math.max(0, nomorKartu.length() - 4));
        return "Kartu kredit " + masked + " berhasil didebet.\n";
    }

    @Override
    public double hitungCicilan(int tenorBulan) {
        double bunga = 0.02; // 2% per bulan (contoh)
        return (jumlah + jumlah * bunga * tenorBulan) / tenorBulan;
    }

    @Override
    public String batalkan() {
        status = "DIBATALKAN";
        return "Transaksi kartu kredit dibatalkan. Dana akan dikembalikan 3-5 hari kerja.\n";
    }
}

// Hanya mendukung pembatalan
class TransferBank extends Pembayaran implements DapatDibatalkan {
    private String bank;
    private String noRekening;

    public TransferBank(String id, double jumlah, String bank, String noRekening) {
        super(id, jumlah);
        this.bank = bank;
        this.noRekening = noRekening;
    }

    @Override
    public String prosesPembayaran() {
        status = "BERHASIL";
        return "Transfer via " + bank + " ke rekening " + noRekening + " berhasil.\n";
    }

    @Override
    public String batalkan() {
        status = "DIBATALKAN";
        return "Transfer bank dibatalkan. Saldo dikembalikan ke rekening asal.\n";
    }
}

// Tidak mendukung cicilan maupun pembatalan
class EWallet extends Pembayaran {
    private String provider;
    private String nomorHp;

    public EWallet(String id, double jumlah, String provider, String nomorHp) {
        super(id, jumlah);
        this.provider = provider;
        this.nomorHp = nomorHp;
    }

    @Override
    public String prosesPembayaran() {
        status = "BERHASIL";
        return "Pembayaran " + provider + " (" + nomorHp + ") berhasil.\n";
    }
}

/* ===================== GUI (SWING + AWT) ===================== */

public class SistemPembayaran extends JFrame implements ActionListener {
    private static final String KARTU = "Kartu Kredit";
    private static final String BANK = "Transfer Bank";
    private static final String WALLET = "E-Wallet";

    private JComboBox<String> cbMetode = new JComboBox<>(new String[]{KARTU, BANK, WALLET});
    private JTextField tfJumlah = new JTextField(15);
    private JTextField tfNoKartu = new JTextField(15);
    private JComboBox<String> cbBank = new JComboBox<>(new String[]{"BCA", "BNI", "BRI", "Mandiri"});
    private JTextField tfRekening = new JTextField(15);
    private JComboBox<String> cbWallet = new JComboBox<>(new String[]{"GoPay", "OVO", "DANA", "ShopeePay"});
    private JTextField tfHp = new JTextField(15);
    private JComboBox<Integer> cbTenor = new JComboBox<>(new Integer[]{3, 6, 12});

    private JButton btnBayar = new JButton("Bayar");
    private JButton btnCicil = new JButton("Hitung Cicilan");
    private JButton btnBatal = new JButton("Batalkan");
    private JTextArea taStruk = new JTextArea(12, 38);

    private JPanel panelDetail = new JPanel(new CardLayout());
    private Pembayaran pembayaranAktif;
    private int counter = 1;

    public SistemPembayaran() {
        super("Sistem Pembayaran");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int pilih = JOptionPane.showConfirmDialog(SistemPembayaran.this,
                        "Yakin ingin keluar?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
                if (pilih == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        // ---- Form input ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Data Pembayaran"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Metode"), g);
        g.gridx = 1; form.add(cbMetode, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Jumlah (Rp)"), g);
        g.gridx = 1; form.add(tfJumlah, g);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; form.add(panelDetail, g);

        panelDetail.add(buatPanel("No. Kartu", tfNoKartu, null, null), KARTU);
        panelDetail.add(buatPanel("Bank", cbBank, "No. Rekening", tfRekening), BANK);
        panelDetail.add(buatPanel("Provider", cbWallet, "No. HP", tfHp), WALLET);

        // ---- Tombol ----
        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        aksi.add(btnBayar);
        aksi.add(new JLabel("Tenor (bln):"));
        aksi.add(cbTenor);
        aksi.add(btnCicil);
        aksi.add(btnBatal);

        btnBayar.setBackground(new Color(46, 125, 50));
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setOpaque(true);
        btnBayar.setBorderPainted(false);
        btnBatal.setForeground(new Color(183, 28, 28));

        JPanel atas = new JPanel(new BorderLayout());
        atas.add(form, BorderLayout.CENTER);
        atas.add(aksi, BorderLayout.SOUTH);

        // ---- Area struk ----
        taStruk.setEditable(false);
        taStruk.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(taStruk);
        sp.setBorder(BorderFactory.createTitledBorder("Struk / Log"));

        add(atas, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        // ---- Event ----
        cbMetode.addActionListener(this);
        btnBayar.addActionListener(this);
        btnCicil.addActionListener(this);
        btnBatal.addActionListener(this);

        perbaruiTombol();
        pack();
        setLocationRelativeTo(null);
    }

    // Override method dari interface ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {
        Object sumber = e.getSource();
        if (sumber == cbMetode) {
            ((CardLayout) panelDetail.getLayout()).show(panelDetail, (String) cbMetode.getSelectedItem());
            pembayaranAktif = null;
            perbaruiTombol();
        } else if (sumber == btnBayar) {
            bayar();
        } else if (sumber == btnCicil) {
            cicil();
        } else if (sumber == btnBatal) {
            batal();
        }
    }

    private JPanel buatPanel(String l1, JComponent c1, String l2, JComponent c2) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; p.add(new JLabel(l1), g);
        g.gridx = 1; p.add(c1, g);
        if (l2 != null) {
            g.gridx = 0; g.gridy = 1; p.add(new JLabel(l2), g);
            g.gridx = 1; p.add(c2, g);
        }
        return p;
    }

    private void bayar() {
        double jumlah;
        try {
            jumlah = Double.parseDouble(tfJumlah.getText().trim());
            if (jumlah <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka lebih dari 0.",
                    "Input tidak valid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = String.format("TRX-%04d", counter);
        String metode = (String) cbMetode.getSelectedItem();
        Pembayaran p;

        if (KARTU.equals(metode)) {
            String no = tfNoKartu.getText().replaceAll("\\s", "");
            if (no.length() < 4) { peringatan("Nomor kartu minimal 4 digit."); return; }
            p = new PembayaranKartuKredit(id, jumlah, no);
        } else if (BANK.equals(metode)) {
            String rek = tfRekening.getText().trim();
            if (rek.isEmpty()) { peringatan("No. rekening wajib diisi."); return; }
            p = new TransferBank(id, jumlah, (String) cbBank.getSelectedItem(), rek);
        } else {
            String hp = tfHp.getText().trim();
            if (hp.isEmpty()) { peringatan("No. HP wajib diisi."); return; }
            p = new EWallet(id, jumlah, (String) cbWallet.getSelectedItem(), hp);
        }

        counter++;
        pembayaranAktif = p;
        taStruk.setText(p.prosesPembayaran() + "\n" + p.cetakStruk());
        perbaruiTombol();
    }

    private void cicil() {
        // Pengecekan interface saat runtime
        if (pembayaranAktif instanceof DapatDicicil) {
            int tenor = (Integer) cbTenor.getSelectedItem();
            double perBulan = ((DapatDicicil) pembayaranAktif).hitungCicilan(tenor);
            taStruk.append("\nCicilan " + tenor + " bulan: " + Pembayaran.formatRupiah(perBulan) + " / bulan\n");
        }
    }

    private void batal() {
        if (pembayaranAktif instanceof DapatDibatalkan) {
            String pesan = ((DapatDibatalkan) pembayaranAktif).batalkan();
            taStruk.setText(pesan + "\n" + pembayaranAktif.cetakStruk());
            perbaruiTombol();
        }
    }

    // Tombol cicil/batal hanya aktif bila metode mendukung interface terkait
    private void perbaruiTombol() {
        boolean berhasil = pembayaranAktif != null && "BERHASIL".equals(pembayaranAktif.getStatus());
        btnCicil.setEnabled(pembayaranAktif instanceof DapatDicicil && berhasil);
        cbTenor.setEnabled(btnCicil.isEnabled());
        btnBatal.setEnabled(pembayaranAktif instanceof DapatDibatalkan && berhasil);
    }

    private void peringatan(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input tidak valid", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SistemPembayaran().setVisible(true));
    }
}