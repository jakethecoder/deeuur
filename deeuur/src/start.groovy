import groovy.swing.SwingBuilder
import javax.swing.BoxLayout as BL;
import java.util.Date;
import java.lang.Thread;
import java.awt.Image
import java.awt.AWTException
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

class Uur {
	def trayIcon
	def swing
	def frame
	def popupTime = 1000 * 60 * 30
	def minutes
	def jobs = []
	def logFile = 'uur.log'
	def init(){
		//read inifile
		readIni()
		//createGUI
		createGUI()
	}
	def exit(){
		def ini = [
			minutes: minutes,
			logFile: "'$logFile'",
			jobs: swing.task.collect {item -> "'$item'"}
		]
		new File('uur.ini').withWriter { writer ->
			writer << ini 
		}
		System.exit(0)
	}
	def mainLoop(){
		while (true) {
			frame.show()
			sleep popupTime
		}
	}
	def createGUI(){
		swing = new SwingBuilder()
		swing.lookAndFeel("system")
		frame = swing.frame(title:'andys uur') {
			panel{
				boxLayout(axis: BL.PAGE_AXIS)
				panel{
					boxLayout(axis: BL.PAGE_AXIS)
					label(text:'Was machen sie gerade?')
					comboBox(id:'task',items: jobs, editable:true)
					label(text:'Kommentar:')
					textField(id:'comments', columns: 20, 
						actionPerformed: { event ->
							save(event)							
						})
				}
				panel{
					boxLayout(axis: BL.LINE_AXIS)
					button(
						text: 'Speichern',
						actionPerformed: { event ->
							save(event)							
						}
					)
					button(
						text: 'Ignorieren',
						actionPerformed: { event ->
							println event.source.text
							frame.hide()
						}
					)
					button(
						text: 'Beenden', actionPerformed: { event ->
							println event.source.text
							//log(swing.task.selectedItem, swing.comments.text)
							exit()
						}
					)
				}
			}
		}
		frame.pack()
		if (SystemTray.isSupported()) {
			def tray = SystemTray.getSystemTray()
			def image = Toolkit.getDefaultToolkit(). getImage("uhr19.gif")
			def popup = new PopupMenu()
			popup.add(new MenuItem(label:"Exit", actionPerformed:{this.exit()}))
			popup.add(new MenuItem(label:"Show", actionPerformed:{frame.show()}))
			trayIcon = new TrayIcon(image:image, tooltip:"Tray Demo", popup:popup, imageAutoSize:true, actionPerformed:{frame.show()})
			try {
				tray.add(trayIcon)
			} catch (AWTException e) {
				println e
			}
		} else {
			prinltn "System Tray not supported"
		}
	}
	def save(event) {
		println event.source.text
		def found = swing.task.find {
			it == swing.task.selectedItem
		}
		if (!found){
			swing.task.addItem(swing.task.selectedItem)
		}													
		log(swing.task.selectedItem, swing.comments.text)
		frame.hide()
		showMessage(event)
	}
	def log(task, comment){
		def logfile = new File(logFile)
		Date d = new Date(System.currentTimeMillis())
		def line = "$d\t$task\t$comment\n"
		logfile.append(line)
	}
	def showMessage(e){
/*		trayIcon.displayMessage(
			"Action Event",
			"An Action Event Has Been Peformed!",
			TrayIcon.MessageType.INFO)*/
	}
	def readIni(){
		def shell = new GroovyShell()
		def ini = shell.evaluate(new File('uur.ini').text)
		assert ini.minutes > 0
		assert ini.logFile != ''
		jobs = ini.jobs
		minutes = ini.minutes
		popupTime = 1000 * 60 * minutes
		logFile = ini.logFile
	}
}
app = new Uur()
app.init()
app.mainLoop()