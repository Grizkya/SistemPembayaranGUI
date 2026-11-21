# Sistem Pembayaran (GUI)

**Nama :** Ghaitsa Rizky Amalia
**NIM  :** F1D02510052

---

## Deskripsi Singkat

Aplikasi desktop sederhana untuk simulasi pembayaran dengan tiga metode: **Kartu Kredit**, **Transfer Bank**, dan **E-Wallet**. Aplikasi ini dibuat untuk mendemonstrasikan konsep OOP Java (abstract class, inheritance, interface, polymorphism) dengan tampilan GUI memakai **Swing** dan **AWT**.

Fitur utama:
- Memilih metode pembayaran dan mengisi data sesuai metode.
- Memproses pembayaran dan mencetak struk.
- Menghitung cicilan (hanya untuk metode yang mendukung).
- Membatalkan transaksi (hanya untuk metode yang mendukung).

---

## Cara Menjalankan

**Kebutuhan:** JDK 8 atau lebih baru.

1. Simpan file `SistemPembayaran.java` di sebuah folder.
2. Buka terminal/CMD di folder tersebut.
3. Compile:
   ```
   javac SistemPembayaran.java
   ```
4. Jalankan:
   ```
   java SistemPembayaran
   ```

> Nama file harus sama dengan nama class public-nya, yaitu `SistemPembayaran.java`.

Bisa juga dijalankan lewat IDE (VS Code, IntelliJ, NetBeans, Eclipse) dengan membuka file tersebut lalu menekan **Run**.

---

## Penjelasan Kode

### 1. Struktur OOP

| Komponen | Jenis | Keterangan |
|---|---|---|
| `Pembayaran` | Abstract class | Menyimpan atribut umum `idTransaksi`, `jumlah`, `status`. Punya method konkret `cetakStruk()` dan method abstrak `prosesPembayaran()`. |
| `PembayaranKartuKredit` | Subclass | Mengimplementasikan `DapatDicicil` dan `DapatDibatalkan`. Nomor kartu disamarkan, hanya 4 digit terakhir yang tampil. |
| `TransferBank` | Subclass | Hanya mengimplementasikan `DapatDibatalkan`. |
| `EWallet` | Subclass | Tidak mengimplementasikan interface apa pun, jadi tidak bisa dicicil maupun dibatalkan. |
| `DapatDicicil` | Interface | Method `hitungCicilan(int tenorBulan)`. |
| `DapatDibatalkan` | Interface | Method `batalkan()`. |

Kemampuan tiap metode pembayaran:

| Metode | Cicilan | Pembatalan |
|---|---|---|
| Kartu Kredit | Ya | Ya |
| Transfer Bank | Tidak | Ya |
| E-Wallet | Tidak | Tidak |

Rumus cicilan (bunga contoh 2% per bulan):

```
cicilan per bulan = (jumlah + jumlah x 0,02 x tenor) / tenor
```

Angka ditampilkan dengan `String.format("%.2f", ...)`, misalnya `Rp 1500000.00`. Tanda desimal (titik atau koma) mengikuti pengaturan bahasa komputer.

### 2. Komponen GUI

| Syarat | Yang dipakai |
|---|---|
| Top-level container | `JFrame` (class `SistemPembayaran extends JFrame`) |
| Intermediate container | `JPanel`, `JScrollPane` |
| Atomic component | `JLabel`, `JTextField`, `JComboBox`, `JButton`, `JTextArea` |
| Layout | `BorderLayout`, `GridLayout`, `FlowLayout`, `CardLayout` |

Fungsi tiap layout:
- `BorderLayout`: menyusun frame (form di atas, area struk di tengah) dan menggabungkan bagian atas.
- `GridLayout`: menyusun pasangan label dan kolom input secara rapi.
- `FlowLayout`: menyusun deretan tombol.
- `CardLayout`: berganti form detail otomatis sesuai metode yang dipilih (data kartu, data bank, atau data e-wallet).

### 3. Event Listener

- Class `SistemPembayaran` mengimplementasikan interface **`ActionListener`** dan meng-override method `actionPerformed(ActionEvent e)`. Method ini membedakan sumber event lewat `e.getSource()` (combo metode, tombol Bayar, Hitung Cicilan, dan Batalkan).
- **`WindowAdapter`** meng-override `windowClosing()` untuk menampilkan konfirmasi sebelum aplikasi ditutup.

### 4. Polymorphism dan `instanceof`

Objek pembayaran yang aktif disimpan sebagai tipe `Pembayaran`. Saat tombol cicilan atau batal ditekan, program mengecek `instanceof DapatDicicil` atau `instanceof DapatDibatalkan` sebelum memanggil method-nya. Tombol-tombol itu juga dinonaktifkan otomatis bila metode tidak mendukung fitur terkait, atau bila transaksi belum berhasil.

### 5. Validasi Input (`try-catch`)

Pada method `bayar()`, input jumlah diubah dengan `Double.parseDouble()`. Jika isinya bukan angka, atau angkanya nol atau negatif, `NumberFormatException` ditangkap oleh `catch` dan ditampilkan sebagai kotak peringatan. Kolom nomor kartu, nomor rekening, dan nomor HP juga dicek agar tidak kosong.

---

## Hasil

Contoh skenario pengujian dan keluaran yang diharapkan:

**1. Kartu Kredit** (jumlah `1500000`, nomor kartu `1234567890123456`)

```
Kartu kredit **** **** **** 3456 berhasil didebet.

=== STRUK PEMBAYARAN ===
ID Transaksi : TRX-0001
Metode       : PembayaranKartuKredit
Jumlah       : Rp 1500000.00
Status       : BERHASIL
```

- Klik **Hitung Cicilan** dengan tenor 3 bulan menambahkan baris:
  `Cicilan 3 bulan: Rp 530000.00 / bulan`
- Klik **Batalkan** mengubah status menjadi `DIBATALKAN`.

**2. Transfer Bank** (BCA, rekening `1234567890`)

```
Transfer via BCA ke rekening 1234567890 berhasil.
```

Tombol **Batalkan** aktif, tombol **Hitung Cicilan** nonaktif.

**3. E-Wallet** (GoPay, nomor HP `08123456789`)

```
Pembayaran GoPay (08123456789) berhasil.
```

Tombol **Hitung Cicilan** dan **Batalkan** keduanya nonaktif.

**4. Input tidak valid**

Jumlah berisi `abc`, kosong, atau `-100` memunculkan peringatan "Jumlah harus berupa angka lebih dari 0."

### Tangkapan Layar

<img width="548" height="595" alt="image" src="https://github.com/user-attachments/assets/57c3fad4-b099-49d6-843f-b1d63ce31df9" />

<img width="545" height="587" alt="image" src="https://github.com/user-attachments/assets/ee7c5bb9-8b60-406f-8a3a-167aa88e8a3b" />

<img width="547" height="591" alt="image" src="https://github.com/user-attachments/assets/2fff1b7e-f027-4f8b-82bf-ff4e8fc4cb84" />

<img width="562" height="591" alt="image" src="https://github.com/user-attachments/assets/8d2a35b8-b4fa-4662-b2b3-d442ec9c3fe4" />

<img width="566" height="591" alt="image" src="https://github.com/user-attachments/assets/f9b55a19-e29f-48f6-a218-d58458cec9d3" />

<img width="562" height="590" alt="image" src="https://github.com/user-attachments/assets/455caf66-8b99-4b9e-b3ad-916f27b29078" />

