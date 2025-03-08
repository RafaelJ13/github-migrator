# GitHub Repository Migrator

This project allows you to migrate repositories from an old GitHub account to a new one, either for public or private repositories. The process involves automatically creating new repositories on the destination account and copying all the files from the original repository to the new repository.

## Features

- Migrate public or private repositories from an old account to a new one.
- Automatically create new repositories on the destination account.
- Copy files and directories from the original repository to the new one.
- Support for private repositories (sets the repository as private on the destination account).

## Requirements

- JDK 8 or higher
- Maven (for dependency management)
- A GitHub authentication token with access to the GitHub API
- Basic knowledge of Git and GitHub

## How to Use

1. **Clone the repository:**

   ```bash
   git clone https://github.com/your_user/github_migrator.git
   cd github_migrator
   ```

2. **Build the project:**

   If you're using Maven, build the project with the following command:

   ```bash
   mvn clean install
   ```

3. **Run the program:**

   Build and run the application with the command:

   ```bash
   mvn exec:java -Dexec.mainClass="RafaelJ13.github_migrator.Main"
   ```

4. **Enter the required information:**

   During execution, the program will ask for:
    - **GitHub token:** The authentication token you obtained from GitHub.
    - **Current username:** The username of the account where repositories will be created.
    - **Old username:** The username of the account from which repositories will be migrated.
    - **Repository type:** Choose between "public" or "private" to indicate which repositories to migrate.

5. **Wait for the migration:**

   The program will migrate the repositories and their files from the old account to the new one.

## How It Works

1. The program uses the **GitHub API** to access the repositories of an old account and obtain a list of repositories.
2. For each repository, the program:
    - Creates a new repository on the destination account.
    - Clones the files from the original repository.
    - Uploads the files to the new repository.
3. The process continues until all desired repositories are migrated.

## Example Execution

```bash
###########################
GitHub Repository Migrator
###########################

This program allows you to migrate repositories from an old account to a new one.
You can choose to migrate public or private repositories.

Enter your GitHub token: <your_token>
Enter the current username (where repositories will be created): <current_username>
Enter the old username (from where repositories will be copied): <old_username>
Do you want to migrate public or private repositories? (private / public): public

Repository <repo_name> created successfully.
File <file_path> uploaded successfully.
...
```

## Contributing

1. Fork the repository.
2. Create a new branch (`git checkout -b my-feature`).
3. Make the necessary changes and commit (`git commit -am 'Add my feature'`).
4. Push to the remote repository (`git push origin my-feature`).
5. Open a pull request.

## License

This project is licensed under the [MIT License](LICENSE).

