/*
 * Copyright © 2015 The CoDoLa developer team
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.olischmid.codola.latex.commons;

/**
 * Created by oli on 29.01.15.
 */
public enum FileEndings {

    LATEX(".tex"), PDF(".pdf");

    public final String ending;

    private FileEndings(String ending){
        this.ending = ending;
    }

    public String appendFileEnding(String fileWithoutEnding){
        if(fileWithoutEnding.endsWith(ending)) {
            return fileWithoutEnding;
        }
        else {
            String newfileWithoutEnding = null;
            for (FileEndings fileEndings : FileEndings.values()) {
                if(fileWithoutEnding.endsWith(fileEndings.ending)){
                    newfileWithoutEnding = fileWithoutEnding.substring(0, fileWithoutEnding.length()-fileEndings.ending.length());
                    break;
                }
            }
            return (newfileWithoutEnding!=null ? newfileWithoutEnding : fileWithoutEnding) + ending;
        }
    }
}
