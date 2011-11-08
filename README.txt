Pentaho Data Integration Prompt Step Plugin
===========================================

This plugin provides a step for prompting the user running the transformation
for input.  It provides support for prompting via a SWT dialog box with either
push buttons, a combo box, or text input.  Choices may be specified statically
in the transformation or read from an additional input step.  The prompt
message supports formatting to include data from input rows in the prompt.

This plugin is currently limited to prompting graphically via SWT, which makes
it unsuitable for remote transformations and for many transformations run from
the command line.  There is no reason that prompting strategies to address
these needs could not be implemented, they simply have not been implemented
yet.

Installation instructions are available in INSTALL.txt.
Major changes are listed in ChangeLog.txt.
Complete license text is available in COPYING.txt.
A list of desired features is in TODO.txt.

This project is hosted on GitHub at
<https://github.com/kevinoid/pdi-prompt-step-plugin>.  Contributions and
further development is welcomed.
