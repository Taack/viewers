package stp

import attachment.Attachment
import attachment.SshAttachmentFolder
import crew.User
import crew.ssh.helper.RealFoldersCallback
import grails.compiler.GrailsCompileStatic
import grails.util.Pair
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import taack.domain.TaackAttachmentService
import taack.ssh.SshEventRegistry
import taack.ssh.vfs.FileTree

import javax.annotation.PostConstruct

@GrailsCompileStatic
final class StpSshService implements SshEventRegistry.VfsEvent {

    static lazyInit = false

    private final String VFS_FILE_NAME = "stp"

    @Value('${intranet.root}')
    String intranetRoot

    String getOpenDatabaseRootPath() {
        intranetRoot + "/stp/openDatabase"
    }

    String getStorePath() {
        intranetRoot + "/attachment/store"
    }

    String getTmpUploadFolder() {
        intranetRoot + '/stp/tmp'
    }

    TaackAttachmentService taackAttachmentService

    @PostConstruct
    def initVfs() {
        log.info "initVfs start"
        FileUtils.forceMkdir(new File(openDatabaseRootPath))
        FileUtils.forceMkdir(new File(storePath))
        FileUtils.forceMkdir(new File(tmpUploadFolder))
        SshEventRegistry.VfsProvider.initVfsEventProvider(VFS_FILE_NAME, this)
        log.info "initVfs ends"
    }

    private final class StpFolder extends SshAttachmentFolder {

        StpFolder(TaackAttachmentService taackSimpleAttachmentService, User user, String storePath, String uploadFolder) {
            super(taackSimpleAttachmentService, user, storePath, uploadFolder)
        }

        @Override
        Iterable<Pair<Long, String>> getAttachmentAndNames() {
            List<Pair<Long, String>> res = []
            for (Attachment attachment : Attachment.findAllByOriginalNameIlike("%.STEP") as List<Attachment>) {
                String name = attachment.originalName.substring(0, attachment.originalName.lastIndexOf('.')) + '_' + attachment.id + ".STEP"
                res.add new Pair<Long, String>(attachment.id, name)
            }
            for (Attachment attachment : Attachment.findAllByOriginalNameIlike("%.stp") as List<Attachment>) {
                String name = attachment.originalName.substring(0, attachment.originalName.lastIndexOf('.')) + '_' + attachment.id + ".STEP"
                res.add new Pair<Long, String>(attachment.id, name)
            }
            res
        }

        @Override
        boolean isUpdatable(Attachment attachment) {
            return false
        }
    }

    static final class OpenDatabase extends RealFoldersCallback {

        OpenDatabase(User user, String realBaseDir) {
            super(user, realBaseDir)
        }
    }

    @Override
    FileTree initVfsAppEvent(String username) {
        log.info "initVfsAppEvent $username"
        User.withNewSession {
            try {
                User u = User.findByUsernameAndEnabled(username, true)
                if (u) {
                    StpFolder stpFolder = new StpFolder(taackAttachmentService, u, storePath, tmpUploadFolder)
                    FileTree fs = new FileTree(username)
                    fs.root = fs.createBuilder(VFS_FILE_NAME)
                            .addNode(fs.createFolder(stpFolder, "STEPFiles"))
                            .addNode(fs.createFolder(new OpenDatabase(u, openDatabaseRootPath), "OpenDatabase", true, openDatabaseRootPath))
                            .toFolder()
                    return fs
                }
            } catch (e) {
                log.error "cannot create FileTree: ${e.message}"
                e.printStackTrace()
                throw e
            }

            log.warn "initVfsAppEvent failed for $username"
            return null
        }
    }

    @Override
    void closeVfsConnection(String username) {
        log.info "closeVfsConnection $username"
    }
}

// Git LFS not working
// git clone --progress --verbose ssh://localhost:22222//home/auo/AUO32
// ssh -p 22222 auo@localhost
// https://programmingtechie.com/2019/08/18/how-to-implement-an-sftp-server-in-java-spring-boot-using-apache-mina-sshd-part-2-using-public-key-authentication/
// https://stackoverflow.com/questions/15372360/apache-sshd-public-key-authentication
// ssh-keygen -t ed25519
// https://phabricator.wikimedia.org/T276486
// ssh -4 -p 22222 -o PubkeyAcceptedKeyTypes=-ssh-rsa -o IdentityFile=~/.ssh/id_ed25519 10.109.55.95
// https://cryptsus.com/blog/how-to-secure-your-ssh-server-with-public-key-elliptic-curve-ed25519-crypto.html
// sftp -4 -oPubkeyAcceptedKeyTypes=-ssh-rsa -oIdentityFile=~/.ssh/id_ed25519 -P 22222 auo4ever
// export GIT_SSH_COMMAND="ssh -4 -i ~/.ssh/id_ed25519 " ; git lfs clone --progress --verbose ssh://auo4ever:22222//home/auo/AUO56
// ssh-keygen -m pem -t ed25519
