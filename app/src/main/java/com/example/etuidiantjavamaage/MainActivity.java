package com.example.etuidiantjavamaage;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText txtNumEt, txtNom, txtMoyenne;
    private Button btnAjouter, btnModifier, btnSupprimer, btnGraphique;
    private ListView listEtudiants;
    private TextView txtMoyClasse, txtMin, txtMax;
    private ProgressBar progressBar;

    private List<Map<String, Object>> listeEtudiants = new ArrayList<>();
    private StudentAdapter studentAdapter;

    private static final String BASE_URL = "http://192.168.63.219/etudiantAPI/api.php";

    private int selectedStudentId = -1;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        chargerEtudiants();
        setupButtons();

        listEtudiants.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            Map<String, Object> etudiant = listeEtudiants.get(position);
            selectedStudentId = (int) etudiant.get("id");

            txtNumEt.setText(etudiant.get("numero").toString());
            txtNom.setText(etudiant.get("nom").toString());
            txtMoyenne.setText(etudiant.get("moyenne").toString());

            Toast.makeText(this, "✅ " + etudiant.get("nom") + " sélectionné", Toast.LENGTH_SHORT).show();
        });

        btnGraphique.setOnClickListener(v -> afficherHistogrammeVertical());
    }

    private void initViews() {
        txtNumEt = findViewById(R.id.txtNumEt);
        txtNom = findViewById(R.id.txtNom);
        txtMoyenne = findViewById(R.id.txtMoyenne);
        btnAjouter = findViewById(R.id.btnAjouter);
        btnModifier = findViewById(R.id.btnModifier);
        btnSupprimer = findViewById(R.id.btnSupprimer);
        btnGraphique = findViewById(R.id.btnGraphique);
        listEtudiants = findViewById(R.id.listEtudiants);
        txtMoyClasse = findViewById(R.id.txtMoyClasse);
        txtMin = findViewById(R.id.txtMin);
        txtMax = findViewById(R.id.txtMax);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupButtons() {
        btnAjouter.setOnClickListener(v -> ajouterEtudiant());
        btnModifier.setOnClickListener(v -> modifierEtudiant());
        btnSupprimer.setOnClickListener(v -> supprimerEtudiant());
    }

    // Fonction pour obtenir le statut OBS
    private String getStatut(double moyenne) {
        if (moyenne >= 10) {
            return "Admis";
        } else if (moyenne >= 5) {
            return "Redoublant";
        } else {
            return "Exclus";
        }
    }

    // Fonction pour obtenir la couleur du statut
    private int getStatutColor(double moyenne) {
        if (moyenne >= 10) {
            return Color.parseColor("#4CAF50");
        } else if (moyenne >= 5) {
            return Color.parseColor("#FF9800");
        } else {
            return Color.parseColor("#F44336");
        }
    }

    // Fonction pour obtenir l'icône du statut
    private String getStatutIcon(double moyenne) {
        if (moyenne >= 10) {
            return "✅";
        } else if (moyenne >= 5) {
            return "⚠️";
        } else {
            return "❌";
        }
    }

    // Obtenir la couleur de la barre selon la moyenne
    private int getBarColor(double moyenne) {
        if (moyenne >= 16) {
            return Color.parseColor("#2E7D32");
        } else if (moyenne >= 14) {
            return Color.parseColor("#43A047");
        } else if (moyenne >= 12) {
            return Color.parseColor("#7CB342");
        } else if (moyenne >= 10) {
            return Color.parseColor("#FFB300");
        } else if (moyenne >= 8) {
            return Color.parseColor("#FB8C00");
        } else if (moyenne >= 5) {
            return Color.parseColor("#F4511E");
        } else {
            return Color.parseColor("#E53935");
        }
    }

    // AFFICHER L'HISTOGRAMME VERTICAL AGRANDI
    private void afficherHistogrammeVertical() {
        if (listeEtudiants.isEmpty()) {
            Toast.makeText(this, "📊 Aucun étudiant à afficher", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer le conteneur principal
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(20, 20, 20, 20);
        container.setBackgroundColor(Color.WHITE);

        // Titre
        TextView titre = new TextView(this);
        titre.setText("📊 HISTOGRAMME DES MOYENNES");
        titre.setTextSize(22);
        titre.setTypeface(null, Typeface.BOLD);
        titre.setTextColor(Color.parseColor("#2196F3"));
        titre.setGravity(Gravity.CENTER);
        titre.setPadding(0, 0, 0, 20);
        container.addView(titre);

        // Scroll horizontal pour les barres (AGRANDI)
        HorizontalScrollView scrollHorizontal = new HorizontalScrollView(this);
        scrollHorizontal.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 700));

        // Conteneur des barres verticales
        LinearLayout barContainer = new LinearLayout(this);
        barContainer.setOrientation(LinearLayout.HORIZONTAL);
        barContainer.setPadding(10, 30, 10, 20);
        barContainer.setGravity(Gravity.BOTTOM);

        double maxMoyenne = 20;

        // Ajouter chaque barre verticale AGRANDIE
        for (int i = 0; i < listeEtudiants.size(); i++) {
            Map<String, Object> etudiant = listeEtudiants.get(i);
            String nom = (String) etudiant.get("nom");
            double moyenne = (double) etudiant.get("moyenne");

            // Conteneur pour une barre + son label
            LinearLayout barWrapper = new LinearLayout(this);
            barWrapper.setOrientation(LinearLayout.VERTICAL);
            barWrapper.setPadding(12, 0, 12, 0);
            barWrapper.setGravity(Gravity.CENTER_HORIZONTAL);

            // Hauteur de la barre AGRANDIE (max 500px au lieu de 350)
            int barHeight = (int) ((moyenne / maxMoyenne) * 500);
            if (barHeight < 15) barHeight = 15;

            // Largeur de la barre AGRANDIE (80px au lieu de 55)
            int barWidth = 80;

            // Créer la barre verticale
            View bar = new View(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, barHeight);
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(getBarColor(moyenne));

            // Arrondir les coins de la barre
            bar.setElevation(4);

            barWrapper.addView(bar);

            // Ajouter la valeur au-dessus de la barre (AGRANDIE)
            TextView tvValeur = new TextView(this);
            tvValeur.setText(String.format("%.1f", moyenne));
            tvValeur.setTextSize(14);
            tvValeur.setTypeface(null, Typeface.BOLD);
            tvValeur.setTextColor(getBarColor(moyenne));
            tvValeur.setGravity(Gravity.CENTER);
            tvValeur.setPadding(0, 0, 0, 5);
            barWrapper.addView(tvValeur);

            // Ajouter l'icône de statut
            TextView tvIcone = new TextView(this);
            tvIcone.setText(getStatutIcon(moyenne));
            tvIcone.setTextSize(16);
            tvIcone.setGravity(Gravity.CENTER);
            barWrapper.addView(tvIcone);

            // Ajouter le nom en dessous (AGRANDI)
            TextView tvNom = new TextView(this);
            String shortName = nom.length() > 12 ? nom.substring(0, 10) + "..." : nom;
            tvNom.setText(shortName);
            tvNom.setTextSize(11);
            tvNom.setTextColor(Color.BLACK);
            tvNom.setTypeface(null, Typeface.BOLD);
            tvNom.setGravity(Gravity.CENTER);
            tvNom.setPadding(0, 8, 0, 0);
            barWrapper.addView(tvNom);

            barContainer.addView(barWrapper);
        }

        scrollHorizontal.addView(barContainer);
        container.addView(scrollHorizontal);

        // Petite légende simplifiée
        LinearLayout legendLayout = new LinearLayout(this);
        legendLayout.setOrientation(LinearLayout.VERTICAL);
        legendLayout.setPadding(15, 20, 15, 15);
        legendLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));

        TextView legendTitle = new TextView(this);
        legendTitle.setText("🎨 LÉGENDE");
        legendTitle.setTextSize(14);
        legendTitle.setTypeface(null, Typeface.BOLD);
        legendTitle.setTextColor(Color.parseColor("#333333"));
        legendTitle.setPadding(0, 0, 0, 10);
        legendLayout.addView(legendTitle);

        // Légende simplifiée
        String[][] legendItems = {
                {"🟢", "≥ 16", "Excellent"},
                {"🟢", "14-16", "Très bien"},
                {"🟡", "12-14", "Bien"},
                {"🟠", "10-12", "Passable"},
                {"🟠", "8-10", "Insuffisant"},
                {"🔴", "5-8", "Faible"},
                {"🔴", "< 5", "Très faible"}
        };

        LinearLayout legendGrid = new LinearLayout(this);
        legendGrid.setOrientation(LinearLayout.HORIZONTAL);
        legendGrid.setWeightSum(2);

        LinearLayout leftColumn = new LinearLayout(this);
        leftColumn.setOrientation(LinearLayout.VERTICAL);
        leftColumn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout rightColumn = new LinearLayout(this);
        rightColumn.setOrientation(LinearLayout.VERTICAL);
        rightColumn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        for (int i = 0; i < legendItems.length; i++) {
            LinearLayout legendRow = new LinearLayout(this);
            legendRow.setOrientation(LinearLayout.HORIZONTAL);
            legendRow.setPadding(10, 5, 10, 5);

            TextView tvIcon = new TextView(this);
            tvIcon.setText(legendItems[i][0]);
            tvIcon.setTextSize(12);
            legendRow.addView(tvIcon);

            TextView tvRange = new TextView(this);
            tvRange.setText(" " + legendItems[i][1] + " : ");
            tvRange.setTextSize(11);
            tvRange.setTextColor(Color.parseColor("#666666"));
            legendRow.addView(tvRange);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(legendItems[i][2]);
            tvDesc.setTextSize(11);
            tvDesc.setTypeface(null, Typeface.BOLD);
            legendRow.addView(tvDesc);

            if (i < 4) {
                leftColumn.addView(legendRow);
            } else {
                rightColumn.addView(legendRow);
            }
        }

        legendGrid.addView(leftColumn);
        legendGrid.addView(rightColumn);
        legendLayout.addView(legendGrid);

        container.addView(legendLayout);

        // Afficher dans un dialogue
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(container)
                .setPositiveButton("Fermer", null)
                .setNegativeButton("Partager", (d, which) -> partagerGraphique())
                .create();

        dialog.show();

        // Ajuster la taille du dialogue (plus grand)
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    // Partager le graphique
    private void partagerGraphique() {
        StringBuilder graphique = new StringBuilder();
        graphique.append("📊 HISTOGRAMME DES MOYENNES 📊\n\n");

        for (Map<String, Object> etudiant : listeEtudiants) {
            String nom = (String) etudiant.get("nom");
            double moyenne = (double) etudiant.get("moyenne");
            String statut = getStatut(moyenne);

            graphique.append("👨‍🎓 ").append(nom).append("\n");
            graphique.append("   Note: ").append(String.format("%.2f", moyenne)).append("/20\n");
            graphique.append("   Statut: ").append(statut).append("\n");
            graphique.append("   ");
            int barLength = (int) (moyenne);
            for (int i = 0; i < barLength; i++) {
                graphique.append("█");
            }
            graphique.append("\n\n");
        }

        android.content.Intent shareIntent = new android.content.Intent();
        shareIntent.setAction(android.content.Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, graphique.toString());
        shareIntent.setType("text/plain");
        startActivity(android.content.Intent.createChooser(shareIntent, "Partager l'histogramme"));
    }

    // CHARGER LES ÉTUDIANTS
    private void chargerEtudiants() {
        showLoading(true);
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.optBoolean("success", false)) {
                        JSONArray data = jsonResponse.optJSONArray("data");
                        if (data != null) {
                            listeEtudiants.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                Map<String, Object> etudiant = new HashMap<>();
                                etudiant.put("id", obj.optInt("id"));
                                etudiant.put("numero", obj.optString("numero"));
                                etudiant.put("nom", obj.optString("nom"));
                                etudiant.put("moyenne", obj.optDouble("moyenne"));
                                listeEtudiants.add(etudiant);
                            }
                            runOnUiThread(() -> {
                                mettreAJourAffichage();
                                calculerStatistiques();
                                showLoading(false);
                            });
                        }
                    }
                } else {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Erreur HTTP: " + responseCode, Toast.LENGTH_SHORT).show();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // CUSTOM ADAPTER POUR TABLEAU ALIGNÉ
    private class StudentAdapter extends BaseAdapter {
        private List<Map<String, Object>> students;

        StudentAdapter(List<Map<String, Object>> students) {
            this.students = students;
        }

        @Override
        public int getCount() {
            return students.size();
        }

        @Override
        public Object getItem(int position) {
            return students.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout;

            if (convertView == null) {
                layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setPadding(16, 12, 16, 12);
                layout.setBackgroundColor(position % 2 == 0 ? 0xFFFFFFFF : 0xFFF5F5F5);

                TextView tvNumero = new TextView(MainActivity.this);
                LinearLayout.LayoutParams numeroParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f);
                tvNumero.setLayoutParams(numeroParams);
                tvNumero.setTextSize(13);
                tvNumero.setTextColor(Color.BLACK);
                tvNumero.setTypeface(null, Typeface.BOLD);

                TextView tvNom = new TextView(MainActivity.this);
                LinearLayout.LayoutParams nomParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.45f);
                tvNom.setLayoutParams(nomParams);
                tvNom.setTextSize(13);
                tvNom.setTextColor(Color.BLACK);

                TextView tvStatut = new TextView(MainActivity.this);
                LinearLayout.LayoutParams statutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f);
                tvStatut.setLayoutParams(statutParams);
                tvStatut.setTextSize(13);
                tvStatut.setTypeface(null, Typeface.BOLD);

                layout.addView(tvNumero);
                layout.addView(tvNom);
                layout.addView(tvStatut);

                layout.setTag(new ViewHolder(tvNumero, tvNom, tvStatut));
            } else {
                layout = (LinearLayout) convertView;
            }

            ViewHolder holder = (ViewHolder) layout.getTag();

            Map<String, Object> etudiant = students.get(position);
            String numero = etudiant.get("numero").toString();
            String nom = etudiant.get("nom").toString();
            double moyenne = (double) etudiant.get("moyenne");

            holder.tvNumero.setText(numero);
            holder.tvNom.setText(nom);
            holder.tvStatut.setText(getStatutIcon(moyenne) + " " + getStatut(moyenne));
            holder.tvStatut.setTextColor(getStatutColor(moyenne));

            return layout;
        }

        private class ViewHolder {
            TextView tvNumero;
            TextView tvNom;
            TextView tvStatut;

            ViewHolder(TextView tvNumero, TextView tvNom, TextView tvStatut) {
                this.tvNumero = tvNumero;
                this.tvNom = tvNom;
                this.tvStatut = tvStatut;
            }
        }

        public void updateData(List<Map<String, Object>> newData) {
            this.students = newData;
            notifyDataSetChanged();
        }
    }

    // METTRE À JOUR L'AFFICHAGE
    private void mettreAJourAffichage() {
        if (studentAdapter == null) {
            studentAdapter = new StudentAdapter(listeEtudiants);
            listEtudiants.setAdapter(studentAdapter);
        } else {
            studentAdapter.updateData(listeEtudiants);
        }
    }

    // CALCULER LES STATISTIQUES
    private void calculerStatistiques() {
        if (listeEtudiants.isEmpty()) {
            txtMoyClasse.setText("0.00");
            txtMin.setText("0.00");
            txtMax.setText("0.00");
            return;
        }

        double somme = 0;
        double min = 20;
        double max = 0;
        int admis = 0, redoublants = 0, exclus = 0;

        for (Map<String, Object> etudiant : listeEtudiants) {
            double moyenne = (double) etudiant.get("moyenne");
            somme += moyenne;
            if (moyenne < min) min = moyenne;
            if (moyenne > max) max = moyenne;

            if (moyenne >= 10) admis++;
            else if (moyenne >= 5) redoublants++;
            else exclus++;
        }

        double moyenneClasse = somme / listeEtudiants.size();

        txtMoyClasse.setText(String.format("%.2f", moyenneClasse));
        txtMin.setText(String.format("%.2f", min));
        txtMax.setText(String.format("%.2f", max));
    }

    // AJOUTER UN ÉTUDIANT
    private void ajouterEtudiant() {
        String numero = txtNumEt.getText().toString().trim();
        String nom = txtNom.getText().toString().trim();
        String moyenneStr = txtMoyenne.getText().toString().trim();

        if (numero.isEmpty() || nom.isEmpty() || moyenneStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        double moyenne;
        try {
            moyenne = Double.parseDouble(moyenneStr);
            if (moyenne < 0 || moyenne > 20) {
                Toast.makeText(this, "⚠️ La moyenne doit être entre 0 et 20", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ Moyenne invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("numero", numero);
            jsonData.put("nom", nom);
            jsonData.put("moyenne", moyenne);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(jsonData.toString().getBytes());
                os.flush();
                os.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "✅ Étudiant ajouté", Toast.LENGTH_SHORT).show();
                        viderChamps();
                        chargerEtudiants();
                    });
                } else {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "❌ Erreur ajout", Toast.LENGTH_SHORT).show();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // MODIFIER UN ÉTUDIANT
    private void modifierEtudiant() {
        if (selectedStudentId == -1) {
            Toast.makeText(this, "⚠️ Sélectionnez un étudiant", Toast.LENGTH_SHORT).show();
            return;
        }

        String nom = txtNom.getText().toString().trim();
        String moyenneStr = txtMoyenne.getText().toString().trim();

        if (nom.isEmpty() || moyenneStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Remplissez les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        double moyenne;
        try {
            moyenne = Double.parseDouble(moyenneStr);
            if (moyenne < 0 || moyenne > 20) {
                Toast.makeText(this, "⚠️ Moyenne entre 0 et 20", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ Moyenne invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("nom", nom);
            jsonData.put("moyenne", moyenne);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "?id=" + selectedStudentId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(jsonData.toString().getBytes());
                os.flush();
                os.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "✅ Étudiant modifié", Toast.LENGTH_SHORT).show();
                        viderChamps();
                        selectedStudentId = -1;
                        chargerEtudiants();
                    });
                } else {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "❌ Erreur modification", Toast.LENGTH_SHORT).show();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // SUPPRIMER UN ÉTUDIANT
    private void supprimerEtudiant() {
        if (selectedStudentId == -1) {
            Toast.makeText(this, "⚠️ Sélectionnez un étudiant", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Supprimer cet étudiant ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    showLoading(true);
                    new Thread(() -> {
                        try {
                            URL url = new URL(BASE_URL + "?id=" + selectedStudentId);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("DELETE");

                            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                runOnUiThread(() -> {
                                    showLoading(false);
                                    Toast.makeText(MainActivity.this, "✅ Étudiant supprimé", Toast.LENGTH_SHORT).show();
                                    viderChamps();
                                    selectedStudentId = -1;
                                    chargerEtudiants();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    showLoading(false);
                                    Toast.makeText(MainActivity.this, "❌ Erreur suppression", Toast.LENGTH_SHORT).show();
                                });
                            }
                            conn.disconnect();
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(MainActivity.this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void viderChamps() {
        txtNumEt.setText("");
        txtNom.setText("");
        txtMoyenne.setText("");
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnAjouter.setEnabled(!show);
        btnModifier.setEnabled(!show);
        btnSupprimer.setEnabled(!show);
        btnGraphique.setEnabled(!show);
    }
}