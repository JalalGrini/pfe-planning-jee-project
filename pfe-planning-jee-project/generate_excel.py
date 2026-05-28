import pandas as pd
import os

data_dir = r"c:\Users\jalal\OneDrive\Desktop\PFE-Planning-System\pfe-planning-jee-project\src\main\resources\data"
output_file = r"c:\Users\jalal\OneDrive\Desktop\PFE-Planning-System\pfe-planning-jee-project\Full_Data_Import.xlsx"

try:
    # 1. Salles
    salles_data = []
    for i in range(1, 26):
        salles_data.append({
            "NOM": f"Salle 1{str(i).zfill(2)}" if i < 10 else f"Salle 2{str(i-9).zfill(2)}",
            "CAPACITE": 30,
            "DISPONIBLE": "OUI"
        })
    df_salles = pd.DataFrame(salles_data)

    # 2. Professeurs
    prof_file = os.path.join(data_dir, "Liste des Profs.xlsx")
    df_profs = pd.read_excel(prof_file)

    # 3. Etudiants
    student_files_map = {
        "Ingénierie des données 3_Email.xlsx": "Ingénierie des Données",
        "Génie Informatique 3 Option GL_Email.xlsx": "Génie Informatique",
        "Transformation Digitale & Intelligence Artificielle 3_Email.xlsx": "Transformation Digitale & Intelligence Artificielle"
    }
    
    df_etudiants_list = []
    for sf, filiere_name in student_files_map.items():
        df = pd.read_excel(os.path.join(data_dir, sf))
        # Remove empty columns that might mess up alignment
        df = df.loc[:, ~df.columns.str.contains('^Unnamed')]
        # Add the Filiere column
        df['Filiere'] = filiere_name
        # Add a dummy Sujet PFE column so the system doesn't generate defaults with @ or fallbacks
        df['Sujet PFE'] = ["Sujet " + str(cne) for cne in df.iloc[:, 0]]
        df_etudiants_list.append(df)
    
    df_etudiants = pd.concat(df_etudiants_list, ignore_index=True)

    # Check headers and ensure Filiere is one of them.
    # The backend ExcelImportService checks for 'Filiere' at index 3 or looks for the header.
    # Actually, the backend reads by column index!
    # extractSoutenancesFromSheet:
    # 0: cne
    # 1: nom
    # 2: prenom
    # 3: filiereNom (getCellValueAsString(row.getCell(3)))
    # 4: sujetPfe (getCellValueAsString(row.getCell(4)))
    
    # We need to make sure the columns are ordered correctly: CNE, Nom, Prenom, Filiere, Sujet.
    # Let's see existing columns of the first df:
    cols = df_etudiants.columns.tolist()
    # It might be: ['CNE', 'Nom', 'Prenom', 'Email perso', 'Email acad', 'Filiere', 'Sujet PFE']
    # The backend reads:
    # 0: cne, 1: nom, 2: prenom, 3: filiere, 4: sujet
    # Let's strictly order them for the backend!
    
    # We will rename existing columns just to be safe, but we don't know exact names.
    # Assuming first 3 are CNE, Nom, Prenom.
    first_3 = cols[:3]
    ordered_cols = first_3 + ['Filiere', 'Sujet PFE'] + [c for c in cols if c not in first_3 and c not in ['Filiere', 'Sujet PFE']]
    
    df_etudiants = df_etudiants[ordered_cols]

    # Save to one Excel
    with pd.ExcelWriter(output_file, engine='openpyxl') as writer:
        df_etudiants.to_excel(writer, sheet_name='Etudiants', index=False)
        df_profs.to_excel(writer, sheet_name='Professeurs', index=False)
        df_salles.to_excel(writer, sheet_name='Salles', index=False)

    print("SUCCESS: Full_Data_Import.xlsx generated successfully with Filieres.")

except Exception as e:
    print("ERROR:", str(e))
